package online.kheops.zipper;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.Set;

public final class InstanceRetriever {
    private static final int CONCURRENT_REQUESTS = 6;

    private final Set<Instance> instances;
    private final String token;
    private final TokenType tokenType;
    private final URI wadoURI;
    private final Client client;

    private boolean started = false;

    private final StateManager stateManager;

    public enum TokenType {
        JWT_BEARER_TOKEN("urn:ietf:params:oauth:grant-type:jwt-bearer"),
        CAPABILITY_TOKEN("urn:x-kheops:params:oauth:grant-type:capability");

        final private String tokenType;

        TokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        @Override
        public String toString() {
            return tokenType;
        }
    }

    public static final class Builder {
        private Set<Instance> instances;
        private String token;
        private TokenType tokenType;
        private URI wadoURI;
        private Client client;

        public Builder instances(Set<Instance> instances) {
            this.instances = instances;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder tokenType(TokenType tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder wadoURI(URI wadoURI) {
            this.wadoURI = wadoURI;
            return this;
        }

        public Builder client(Client client) {
            this.client = client;
            return this;
        }

        public InstanceRetriever build() {
            if (instances == null) {
                throw new IllegalStateException("instances not set");
            }
            if (token == null) {
                throw new IllegalStateException("token not set");
            }
            if (tokenType == null) {
                throw new IllegalStateException("tokenType not set");
            }
            if (wadoURI == null) {
                throw new IllegalStateException("wadoURI not set");
            }
            if (client == null) {
                throw new IllegalStateException("client not set");
            }

            return new InstanceRetriever(this);
        }
    }

    private InstanceRetriever(Builder builder) {
        instances = builder.instances;
        token = builder.token;
        tokenType = builder.tokenType;
        wadoURI = builder.wadoURI;
        client = builder.client;

        stateManager = StateManager.newInstance(instances);
    }

    public void start() {
        started = true;

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            startNewRequest();
        }
    }

    public InstanceData nextInstance() {
        if (!started) {
            throw new IllegalStateException("InstanceRetriever not started");
        }

        InstanceData instanceData = null;
        while (stateManager.countUnReturned() > 0 && (instanceData = stateManager.getForReturning()) == null) {
            synchronized(stateManager) {
                try {
                    stateManager.wait(1000);
                } catch (InterruptedException e) { /* empty */ }
            }
        }

        return instanceData;
    }

    private void startNewRequest() {

    }

}






