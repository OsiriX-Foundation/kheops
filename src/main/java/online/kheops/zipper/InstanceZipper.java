package online.kheops.zipper;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class InstanceZipper {
    private final InstanceRetrievalService instanceRetrievalService;
    private final int instanceCount;
    private final StreamingOutput streamingOutput = new ZipperStreamingOutput();

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

                ZipEntry e = new ZipEntry(getZipEntryName(instanceFuture.getInstance()));
                zipStream.putNextEntry(e);
                zipStream.write(instanceBytes);
                zipStream.closeEntry();
            }
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
        return streamingOutput;
    }
}
