package online.kheops.zipper;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.util.UIDUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

@SuppressWarnings("WeakerAccess")
public class DicomDirGenerator implements Closeable {
    private static final String DICOMDIR_FILENAME = "DICOMDIR";
    private static final String DEFAULT_FILESET_ID = "Dicom Files";

    private final File file;
    private final DicomDirWriter dicomDirWriter;
    private final RecordFactory recordFactory = new RecordFactory();

    private final Object lock = new Object();
    private static final Logger LOG = Logger.getLogger(DicomDirGenerator.class.getName());

    private class AddInstanceCallable implements Callable<Void> {
        private final byte[] instanceBytes;
        private final Path path;

        private AddInstanceCallable(byte[] instanceBytes, Path path) {
            this.instanceBytes = instanceBytes;
            this.path = path;
        }

        @Override
        public Void call() throws Exception {
            addInstance(instanceBytes, path);
            return null;
        }
    }

    private DicomDirGenerator(String fileSetId) throws IOException {
        file = File.createTempFile(DICOMDIR_FILENAME, null);
        DicomDirWriter.createEmptyDirectory(file,
                UIDUtils.createUID(),
                fileSetId,
                null,
                null);
        dicomDirWriter = DicomDirWriter.open(file);
        dicomDirWriter.setEncodingOptions(DicomEncodingOptions.DEFAULT);
    }

    public static DicomDirGenerator getInstance() throws IOException {
        return new DicomDirGenerator(DEFAULT_FILESET_ID);
    }

    public Callable<Void> getAddInstanceCallable(byte[] instanceBytes, Path path) {
        return new AddInstanceCallable(instanceBytes, path);
    }

    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public void close() throws IOException {
        dicomDirWriter.close();
        if (!file.delete()) {
            LOG.warning("Unable to delete the temporary DICOMDIR file");
        }
    }

    private void addInstance(byte[] instanceBytes, Path path) throws Exception {
        synchronized (lock) {
            Attributes fmi;
            Attributes dataset;
            try (DicomInputStream din = new DicomInputStream(new ByteArrayInputStream(instanceBytes))) {
                din.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
                fmi = din.readFileMetaInformation();
                dataset = din.readDataset(-1, Tag.PixelData);
            }
            if (fmi == null) {
                fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian);
            }
            final String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
            if (iuid == null) {
                throw new IllegalArgumentException("No MediaStorageSOPInstanceUIDin the instance");
            }

            String patientID = dataset.getString(Tag.PatientID, null);
            final String studyUID = dataset.getString(Tag.StudyInstanceUID, null);
            final String seriesUID = dataset.getString(Tag.SeriesInstanceUID, null);
            final String[] fileIDs = StreamSupport.stream(path.spliterator(), false)
                    .map(Path::toString)
                    .toArray(String[]::new);

            if (studyUID == null) {
                throw new IllegalArgumentException("No StudyInstanceUID the instance");
            }
            if (seriesUID == null) {
                throw new IllegalArgumentException("No SeriesInstanceUID the instance");
            }

            if (patientID == null) {
                dataset.setString(Tag.PatientID, VR.LO, patientID = studyUID);
            }
            Attributes patientRecord = dicomDirWriter.findPatientRecord(patientID);
            if (patientRecord == null) {
                patientRecord = recordFactory.createRecord(RecordType.PATIENT, null,
                        dataset, null, null);
                dicomDirWriter.addRootDirectoryRecord(patientRecord);
            }
            Attributes studyRecord = dicomDirWriter.findStudyRecord(patientRecord, studyUID);
            if (studyRecord == null) {
                studyRecord = recordFactory.createRecord(RecordType.STUDY, null,
                        dataset, null, null);
                dicomDirWriter.addLowerDirectoryRecord(patientRecord, studyRecord);
            }

            Attributes seriesRecord = dicomDirWriter.findSeriesRecord(studyRecord, seriesUID);
            if (seriesRecord == null) {
                seriesRecord = recordFactory.createRecord(RecordType.SERIES, null,
                        dataset, null, null);
                dicomDirWriter.addLowerDirectoryRecord(studyRecord, seriesRecord);
            }

            Attributes instanceRecord = recordFactory.createRecord(dataset, fmi, fileIDs);
            dicomDirWriter.addLowerDirectoryRecord(seriesRecord, instanceRecord);
        }
    }
}
