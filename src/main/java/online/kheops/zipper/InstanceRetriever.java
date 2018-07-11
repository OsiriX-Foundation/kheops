package online.kheops.zipper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public final class InstanceRetriever {
    private static final int CONCURRENT_REQUESTS = 6;
    private static final long SLEEP_TIME = 500;

    private static final Logger LOG = Logger.getLogger(InstanceRetriever.class.getName());

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

        public InstanceRetriever build() {
            return new InstanceRetriever(this);
        }
    }

    private InstanceRetriever(Builder builder) {
        wadoURI = Objects.requireNonNull(builder.wadoURI, "wadoURI must not be null");
        client = Objects.requireNonNull(builder.client, "client must not be null");

        stateManager = StateManager.newInstance(builder.instances);
        bearerTokenRetriever = new BearerTokenRetriever.Builder().accessToken(builder.accessToken).authorizationURI(builder.authorizationURI).client(client).build();
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

    public InstanceData poll() {
        if (!started) {
            throw new IllegalStateException("InstanceRetriever not started");
        }

        InstanceData instanceData = null;
        while (stateManager.countUnReturned() > 0 && (instanceData = stateManager.getForReturning()) == null) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) { /* empty */ }
        }

        return instanceData;
    }

    private void startNewRequest() {
        final Instance instance = stateManager.getForProcessing();

        bearerTokenRetriever.get(instance, new InvocationCallback<BearerToken>() {
            @Override
            public void completed(BearerToken bearerToken) {
                processInstance(instance, bearerToken);
            }

            @Override
            public void failed(Throwable throwable) {
                stateManager.failedProcessing(instance);
                LOG.log(Level.WARNING, "Failed to get a Bearer token for Study:" + instance.getStudyInstanceUID() +
                        "%nSeries:" + instance.getSeriesInstanceUID() + "%nInstance:" + instance.getSOPInstanceUID(), throwable);
                startNewRequest();
            }
        });
    }

    private void processInstance(Instance instance, BearerToken bearerToken) {
        URI instanceURI = getWadoUriBuilder().queryParam("studyUID", instance.getStudyInstanceUID())
                .queryParam("seriesUID", instance.getSeriesInstanceUID())
                .queryParam("objectUID", instance.getSOPInstanceUID())
                .build();

        client.target(instanceURI)
                .request("application/dicom")
                .header("Authorization", "Bearer " + bearerToken.getToken())
                .async()
                .get(new InvocationCallback<byte[]>() {
                    @Override
                    public void completed(byte[] bytes) {
                        stateManager.finishedProcessing(InstanceData.newInstance(instance, bytes));
                        startNewRequest();
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        stateManager.failedProcessing(instance);
                        LOG.log(Level.WARNING, "Failed to process Study:" + instance.getStudyInstanceUID() +
                                "%nSeries:" + instance.getSeriesInstanceUID() + "%nInstance:" + instance.getSOPInstanceUID(), throwable);
                        startNewRequest();
                    }
                });
    }

    private UriBuilder getWadoUriBuilder() {
        return UriBuilder.fromUri(wadoURI).path("/wado")
                .queryParam("requestType", "WADO")
                .queryParam("contentType", "application%2Fdicom")
                .queryParam("transferSyntax", "*");
    }
}
