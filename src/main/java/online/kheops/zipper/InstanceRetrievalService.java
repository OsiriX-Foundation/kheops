package online.kheops.zipper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public final class InstanceRetrievalService {
    private static final int CONCURRENT_REQUESTS = 6;

    private final URI wadoURI;
    private final Client client;

    private boolean started = false;

    private final StateManager stateManager;
    private final BearerTokenRetriever bearerTokenRetriever;

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

        public InstanceRetrievalService build() {
            return new InstanceRetrievalService(this);
        }
    }

    private InstanceRetrievalService(Builder builder) {
        wadoURI = Objects.requireNonNull(builder.wadoURI, "wadoURI");
        client = Objects.requireNonNull(builder.client, "client");

        stateManager = StateManager.newInstance(builder.instances);
        bearerTokenRetriever = new BearerTokenRetriever.Builder()
                .accessToken(builder.accessToken)
                .authorizationURI(builder.authorizationURI)
                .client(client)
                .build();
    }

    public void start() {
        if (started) {
            throw new IllegalStateException("Already Started");
        }

        started = true;

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            startNewRequest();
        }
    }

    public InstanceFuture take() throws InterruptedException {
        if (!started) {
            throw new IllegalStateException("Not Started");
        }

        InstanceFuture instanceFuture = stateManager.take();

        startNewRequest();

        return instanceFuture;
    }

    private void startNewRequest() {
        final Instance instance = stateManager.getForProcessing();

        if (instance == null) {
            return;
        }

        bearerTokenRetriever.get(instance, new InvocationCallback<BearerToken>() {
            @Override
            public void completed(BearerToken bearerToken) {
                processInstance(instance, bearerToken);
            }

            @Override
            public void failed(Throwable throwable) {
                failedProcessing(instance, throwable);
            }
        });
    }

    private void processInstance(Instance instance, BearerToken bearerToken) {
        URI instanceURI = getInstanceURI(instance);

        client.target(instanceURI)
                .request("application/dicom")
                .header("Authorization", "Bearer " + bearerToken.getToken())
                .async()
                .get(new InvocationCallback<byte[]>() {
                    @Override
                    public void completed(byte[] bytes) {
                        finishedProcessing(instance, bytes);
                    }

                    @Override
                    public void failed(Throwable reason) {
                        failedProcessing(instance, reason);
                    }
                });
    }

    private URI getInstanceURI(Instance instance) {
        return getWadoUriBuilder().queryParam("studyUID", instance.getStudyInstanceUID())
                .queryParam("seriesUID", instance.getSeriesInstanceUID())
                .queryParam("objectUID", instance.getSOPInstanceUID())
                .build();
    }

    private UriBuilder getWadoUriBuilder() {
        return UriBuilder.fromUri(wadoURI).path("/wado")
                .queryParam("requestType", "WADO")
                .queryParam("contentType", "application%2Fdicom")
                .queryParam("transferSyntax", "*");
    }

    private void finishedProcessing(Instance instanceData, byte[] bytes) {
        stateManager.finishedProcessing(instanceData, bytes);
    }

    private void failedProcessing(Instance instance, Throwable reason) {
        stateManager.failedProcessing(instance, reason);
        startNewRequest();
    }
}
