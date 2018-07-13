package online.kheops.zipper;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("WeakerAccess")
public final class Zipper implements StreamingOutput {
    private final InstanceCompletionService instanceCompletionService;
    private final int instanceCount;

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

        public Zipper build() {
            return new Zipper(this);
        }
    }

    private Zipper(Builder builder) {
        instanceCompletionService = new InstanceCompletionService.Builder()
                .instances(builder.instances)
                .accessToken(builder.accessToken)
                .wadoURI(builder.wadoURI)
                .authorizationURI(builder.authorizationURI)
                .client(builder.client)
                .build();
        instanceCount = builder.instances.size();
    }

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

            ZipEntry e = new ZipEntry(instanceFuture.getInstance().getSOPInstanceUID() + ".dcm");
            zipStream.putNextEntry(e);
            zipStream.write(instanceBytes);
            zipStream.closeEntry();
        }
        zipStream.close();
    }
}
