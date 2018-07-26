package online.kheops.zipper;

import online.kheops.zipper.dicomdir.DicomDirGenerator;
import online.kheops.zipper.instance.InstanceRetrievalService;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class InstanceZipper {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private final InstanceRetrievalService instanceRetrievalService;
    private boolean alreadyWritten = false;

    private InstanceZipper(InstanceRetrievalService instanceRetrievalService) {
        this.instanceRetrievalService = Objects.requireNonNull(instanceRetrievalService, "instanceRetrievalService");
    }

    public static InstanceZipper newInstance(InstanceRetrievalService instanceRetrievalService) {
        return new InstanceZipper(instanceRetrievalService);
    }

    private final class ZipperStreamingOutput implements StreamingOutput {

        @Override
        public void write(OutputStream outputStream) throws IOException {
            try (final DicomDirGenerator dicomDirGenerator = DicomDirGenerator.getInstance()){
                privateWrite(outputStream, dicomDirGenerator);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted");
            } catch (ExecutionException e) {
                throw new IOException("Unable to get ZIP data", e.getCause());
            }
        }

        private void privateWrite(OutputStream output, DicomDirGenerator dicomDirGenerator) throws IOException, InterruptedException, ExecutionException {
            try (final ZipOutputStream zipStream = new ZipOutputStream(output)) {
                if (alreadyWritten) {
                    throw new IllegalStateException("Already written out");
                }
                alreadyWritten = true;

                instanceRetrievalService.start();
                for (int i = 0; i < instanceRetrievalService.getInstanceCount(); i++) {
                    final byte[] instanceBytes = instanceRetrievalService.take().get();

                    final String instanceFileName = String.format("%08d", i + 1);
                    final Future dicomdirFuture = EXECUTOR_SERVICE.submit(dicomDirGenerator.getAddInstanceCallable(instanceBytes, Paths.get("DICOM", instanceFileName)));

                    zipStream.putNextEntry(new ZipEntry("DICOM/" + instanceFileName));
                    zipStream.write(instanceBytes);
                    zipStream.closeEntry();

                    dicomdirFuture.get();
                }

                zipStream.putNextEntry(new ZipEntry("DICOMDIR"));
                zipStream.write(dicomDirGenerator.getBytes());
                zipStream.closeEntry();
            }
        }
    }

    public StreamingOutput getStreamingOutput() {
        return new ZipperStreamingOutput();
    }
}
