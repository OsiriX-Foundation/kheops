package online.kheops.zipper;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class InstanceZipper {
    private final InstanceRetrievalService instanceCompletionService;
    private final int instanceCount;
    private final StreamingOutput streamingOutput = new ZipperStreamingOutput();

    public static final class Builder {
        private Set<Instance> instances;
        private AccessToken accessToken;
        private URI wadoURI;
        private URI authorizationURI;
        private Client client;

        public Builder(){}

        public Builder instances(Set<Instance> instances) {
            this.instances = instances;
            return this;
        }

        public Builder accessToken(AccessToken accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder wadoURI(URI wadoURI) {
            this.wadoURI = wadoURI;
            return this;
        }

        public Builder authorizationURI(URI authorizationURI) {
            this.authorizationURI = authorizationURI;
            return this;
        }

        public Builder client(Client client) {
            this.client = client;
            return this;
        }

        public InstanceZipper build() {
            return new InstanceZipper(this);
        }
    }

    private class ZipperStreamingOutput implements StreamingOutput {
        public void write(OutputStream output) throws IOException {
            instanceCompletionService.start();

            ZipOutputStream zipStream = new ZipOutputStream(output);

            for (int i = 0; i < instanceCount; i++) {
                final InstanceFuture instanceFuture;
                final byte[] instanceBytes;
                try {
                    instanceFuture = instanceCompletionService.take();
                    instanceBytes = instanceFuture.get();
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
    }

    private InstanceZipper(Builder builder) {
        instanceCompletionService = new InstanceRetrievalService.Builder()
                .instances(builder.instances)
                .accessToken(builder.accessToken)
                .wadoURI(builder.wadoURI)
                .authorizationURI(builder.authorizationURI)
                .client(builder.client)
                .build();
        instanceCount = builder.instances.size();
    }

    private static String getZipEntryName(Instance instance) {
        return instance.getStudyInstanceUID() + "/" + instance.getSeriesInstanceUID() + "/" + instance.getSOPInstanceUID() + ".dcm";
    }

    public StreamingOutput getStreamingOutput() {
        return streamingOutput;
    }
}
