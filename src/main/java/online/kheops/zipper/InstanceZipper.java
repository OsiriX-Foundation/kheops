package online.kheops.zipper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("WeakerAccess")
public final class InstanceZipper {
    private final InstanceRetriever instanceRetriever;

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

    private InstanceZipper(Builder builder) {
        instanceRetriever = new InstanceRetriever.Builder()
                .instances(builder.instances)
                .accessToken(builder.accessToken)
                .wadoURI(builder.wadoURI)
                .authorizationURI(builder.authorizationURI)
                .client(builder.client)
                .build();
    }

    public void write(OutputStream output) throws IOException, WebApplicationException {
        instanceRetriever.start();

        ZipOutputStream zipStream = new ZipOutputStream(output);

        InstanceData instanceData;
        while ((instanceData = instanceRetriever.poll()) != null) {
            byte[] instanceBytes = instanceData.getBytes();

            if (instanceBytes == null) {
                throw new WebApplicationException("Missing data to add to the zip stream", Response.Status.SERVICE_UNAVAILABLE);
            }

            ZipEntry e = new ZipEntry(instanceData.getInstance().getSOPInstanceUID() + ".dcm");
            zipStream.putNextEntry(e);
            zipStream.write(instanceBytes);
            zipStream.closeEntry();
        }
        zipStream.close();
    }
}
