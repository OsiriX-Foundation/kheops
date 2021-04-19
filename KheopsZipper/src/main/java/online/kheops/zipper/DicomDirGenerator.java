package online.kheops.zipper;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.media.DicomDirWriter;
import org.dcm4che3.media.RecordFactory;
import org.dcm4che3.media.RecordType;
import org.dcm4che3.util.UIDUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.StreamSupport;

public final class DicomDirGenerator implements Closeable {
    private static final String DICOMDIR_FILENAME = "DICOMDIR";
    private static final String DEFAULT_FILESET_ID = "KHEOPS_ZIP";

    private final File file;
    private final DicomDirWriter dicomDirWriter;
    private final RecordFactory recordFactory = new RecordFactory();

    private boolean dicomDirWriterClosed = false;
    private boolean closed = false;

    private DicomDirGenerator(String fileSetId) throws IOException {
        file = File.createTempFile(DICOMDIR_FILENAME, null);
        DicomDirWriter.createEmptyDirectory(file,
                UIDUtils.createUID(),
                fileSetId,
                null,
                null);
        dicomDirWriter = DicomDirWriter.open(file);
    }

    public static DicomDirGenerator newInstance() throws IOException {
        return new DicomDirGenerator(DEFAULT_FILESET_ID);
    }

    @Override
    public void close() throws IOException {
        closeDicomDirWriter();
        Files.delete(file.toPath());
        closed = true;
    }

    public void write(final OutputStream outputStream) throws IOException {
        if (closed) {
            throw new IllegalStateException("Can't get bytes when closed");
        }

        closeDicomDirWriter();
        Files.copy(file.toPath(), outputStream);
        outputStream.flush();
    }

    public void add(final InputStream inputStream, final Path path) throws IOException {
        Attributes fmi;
        final Attributes dataset;
        try (DicomInputStream din = new DicomInputStream(inputStream)) {
            din.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            fmi = din.readFileMetaInformation();
            dataset = din.readDataset(-1, Tag.PixelData);
        }
        if (fmi == null) {
            fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian);
        }
        final String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
        if (iuid == null) {
            throw new IllegalArgumentException("No MediaStorageSOPInstanceUID in the instance");
        }

        String patientID = dataset.getString(Tag.PatientID, null);
        final String studyUID = dataset.getString(Tag.StudyInstanceUID, null);
        final String seriesUID = dataset.getString(Tag.SeriesInstanceUID, null);
        final String[] fileIDs = StreamSupport.stream(path.spliterator(), false)
                .map(Path::toString)
                .toArray(String[]::new);

        if (studyUID == null) {
            throw new IllegalArgumentException("No StudyInstanceUID in the instance");
        }
        if (seriesUID == null) {
            throw new IllegalArgumentException("No SeriesInstanceUID in the instance");
        }

        if (patientID == null) {
            patientID = studyUID;
            dataset.setString(Tag.PatientID, VR.LO, patientID);
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

        final Attributes instanceRecord = recordFactory.createRecord(dataset, fmi, fileIDs);
        dicomDirWriter.addLowerDirectoryRecord(seriesRecord, instanceRecord);
    }

    private void closeDicomDirWriter() throws IOException
    {
        if (!dicomDirWriterClosed) {
            dicomDirWriter.close();
            dicomDirWriterClosed = true;
        }
    }

}
