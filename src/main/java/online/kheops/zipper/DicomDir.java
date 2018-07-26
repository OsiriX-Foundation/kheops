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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class DicomDir {
    private final File file;
    private final DicomDirWriter dicomDirWriter;
    private final RecordFactory recordFactory = new RecordFactory();

    private final Object lock = new Object();

    private static final Logger LOG = Logger.getLogger(DicomDir.class.getName());


    private class AddInstanceRunner implements Runnable {
        private final Instance instance;
        private final byte[] instanceBytes;
        private final Path path;

        private AddInstanceRunner(Instance instance, byte[] instanceBytes, Path path) {
            this.instance = instance;
            this.instanceBytes = instanceBytes;
            this.path = path;
        }

        @Override
        public void run() {
            addInstance(instance, instanceBytes, path);
        }
    }

    public static DicomDir getInstance() {
        return new DicomDir();
    }

    private DicomDir() {
        try {
            this.file = File.createTempFile("DICOMDIR", "");
            DicomDirWriter.createEmptyDirectory(file,
                    UIDUtils.createUID(),
                    "FileSet ID",
                    null,
                    null);
            dicomDirWriter = DicomDirWriter.open(file);
            dicomDirWriter.setEncodingOptions(DicomEncodingOptions.DEFAULT);
        } catch (IOException e) {
            throw new IllegalStateException("can't make the new files", e);
        }
    }


    public Runnable getAddInstanceRunnable(Instance instance, byte[] instanceBytes, Path path) {
        return new AddInstanceRunner(instance, instanceBytes, path);
    }

    public byte[] getBytes() {
        try {
            dicomDirWriter.close();
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            LOG.severe("unable to close dicom dir writer" + e.getMessage());
            return new byte[0];
        }
    }

    private void addInstance(Instance instance, byte[] instanceBytes, Path path) {
        synchronized (lock) {
            Attributes fmi;
            Attributes dataset;
            DicomInputStream din = null;
            try {
                din = new DicomInputStream(new ByteArrayInputStream(instanceBytes));
                din.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
                fmi = din.readFileMetaInformation();
                dataset = din.readDataset(-1, Tag.PixelData);
            } catch (IOException e) {
                LOG.severe("failed-to-parse" + instance + e.getMessage());
                return;
            } finally {
                if (din != null) {
                    try {
                        din.close();
                    } catch (Exception e) {
                        LOG.severe("failed to close din" + instance + e.getMessage());
                    }
                }
            }
            if (fmi == null) {
                fmi = dataset.createFileMetaInformation(UID.ImplicitVRLittleEndian);
            }
            String iuid = fmi.getString(Tag.MediaStorageSOPInstanceUID, null);
            if (iuid == null) {
                LOG.severe("No MediaStorageSOPInstanceUID " + instance);
                return;
            }

            String pid = dataset.getString(Tag.PatientID, null);
            String styuid = dataset.getString(Tag.StudyInstanceUID, null);
            String seruid = dataset.getString(Tag.SeriesInstanceUID, null);

            Iterable<Path> iterable = path;
            final String[] fileIDs = StreamSupport.stream(iterable.spliterator(), false)
                    .map(Path::toString)
                    .toArray(String[]::new);

            try {
                if (styuid != null) {
                    if (pid == null) {
                        dataset.setString(Tag.PatientID, VR.LO, pid = styuid);
                    }
                    Attributes patRec = dicomDirWriter.findPatientRecord(pid);
                    if (patRec == null) {
                        patRec = recordFactory.createRecord(RecordType.PATIENT, null,
                                dataset, null, null);
                        dicomDirWriter.addRootDirectoryRecord(patRec);
                    }
                    Attributes studyRec = dicomDirWriter.findStudyRecord(patRec, styuid);
                    if (studyRec == null) {
                        studyRec = recordFactory.createRecord(RecordType.STUDY, null,
                                dataset, null, null);
                        dicomDirWriter.addLowerDirectoryRecord(patRec, studyRec);
                    }

                    if (seruid != null) {
                        Attributes seriesRec = dicomDirWriter.findSeriesRecord(studyRec, seruid);
                        if (seriesRec == null) {
                            seriesRec = recordFactory.createRecord(RecordType.SERIES, null,
                                    dataset, null, null);
                            dicomDirWriter.addLowerDirectoryRecord(studyRec, seriesRec);
                        }

                        Attributes instRec;
                        instRec = recordFactory.createRecord(dataset, fmi, fileIDs);
                        dicomDirWriter.addLowerDirectoryRecord(seriesRec, instRec);
                    }
                } else {
                    Attributes instRec = recordFactory.createRecord(dataset, fmi, fileIDs);
                    dicomDirWriter.addRootDirectoryRecord(instRec);
                }
            } catch (IOException e) {
                LOG.severe("IOException" + instance + e.getMessage());
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (!file.delete()) {
            LOG.warning("Unable to delete the temporary DICOMDIR file");
        }

        super.finalize();
    }
}
