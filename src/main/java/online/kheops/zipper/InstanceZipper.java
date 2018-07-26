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

    public static final class Builder {
        private  InstanceRetrievalService instanceRetrievalService;

        public Builder instanceRetrievalService(InstanceRetrievalService instanceRetrievalService) {
            this.instanceRetrievalService = instanceRetrievalService;
            return this;
        }

        public InstanceZipper build() {
            return new InstanceZipper(this);
        }
    }

    private class ZipperStreamingOutput implements StreamingOutput {
        final DicomDir dicomDir = DicomDir.getInstance();

        @Override
        public void write(OutputStream output) throws IOException {
            instanceRetrievalService.start();

            ZipOutputStream zipStream = new ZipOutputStream(output);

            for (int i = 0; i < instanceCount; i++) {
                final InstanceFuture instanceFuture;
                final byte[] instanceBytes;
                try {
                    instanceFuture = instanceRetrievalService.take();
                    instanceBytes = instanceFuture.get();
                }catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Current thread was interrupted", e);
                } catch (Exception e) {
                    throw new IOException("Missing data to add to the zip stream", e);
                }

                final Future dicomFuture = EXECUTOR_SERVICE.submit(dicomDir.getAddInstanceRunnable(instanceFuture.getInstance(), instanceBytes, Paths.get("DICOM", String.valueOf(i))));

                final ZipEntry entry = new ZipEntry("DICOM/" + String.format("%08d", i));
                zipStream.putNextEntry(entry);
                zipStream.write(instanceBytes);
                zipStream.closeEntry();
                try {
                    dicomFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new IOException("get interupted", e);
                }
            }

            ZipEntry dicomDirEntry = new ZipEntry("DICOMDIR");
            zipStream.putNextEntry(dicomDirEntry);
            zipStream.write(dicomDir.getBytes());
            zipStream.closeEntry();

            zipStream.close();
        }

        private String getZipEntryName(Instance instance) {
            return instance.getStudyInstanceUID() + "/" + instance.getSeriesInstanceUID() + "/" + instance.getSopInstanceUID() + ".dcm";
        }
    }

    private InstanceZipper(Builder builder) {
        instanceRetrievalService = Objects.requireNonNull(builder.instanceRetrievalService, "instanceRetrievalService");
        instanceCount = instanceRetrievalService.getInstanceCount();
    }

    public StreamingOutput getStreamingOutput() {
        return new ZipperStreamingOutput();
    }
}
