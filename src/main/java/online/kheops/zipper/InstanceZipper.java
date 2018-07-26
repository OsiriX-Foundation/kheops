package online.kheops.zipper;

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
    private final int instanceCount;
    private boolean finishedWriting = false;

    public InstanceZipper(InstanceRetrievalService instanceRetrievalService) {
        this.instanceRetrievalService = Objects.requireNonNull(instanceRetrievalService, "instanceRetrievalService");
        instanceCount = instanceRetrievalService.getInstanceCount();
    }

    private class ZipperStreamingOutput implements StreamingOutput {

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
    }

    private void privateWrite(OutputStream output, DicomDirGenerator dicomDirGenerator) throws IOException, InterruptedException, ExecutionException {
        if (finishedWriting) {
            throw new IllegalStateException("Already written out");
        }
        finishedWriting = true;

        instanceRetrievalService.start();

        ZipOutputStream zipStream = new ZipOutputStream(output);

        for (int i = 0; i < instanceCount; i++) {
            final InstanceFuture instanceFuture = instanceRetrievalService.take();
            final byte[] instanceBytes = instanceFuture.get();

            final String instanceFileName = String.format("%08d", i+1);
            final Future dicomFuture = EXECUTOR_SERVICE.submit(dicomDirGenerator.getAddInstanceCallable(instanceBytes, Paths.get("DICOM", instanceFileName)));

            zipStream.putNextEntry(new ZipEntry("DICOM/" + instanceFileName));
            zipStream.write(instanceBytes);
            zipStream.closeEntry();

            dicomFuture.get();
        }

        zipStream.putNextEntry(new ZipEntry("DICOMDIR"));
        zipStream.write(dicomDirGenerator.getBytes());
        zipStream.closeEntry();

        zipStream.close();
    }

    public StreamingOutput getStreamingOutput() {
        return new ZipperStreamingOutput();
    }
}
