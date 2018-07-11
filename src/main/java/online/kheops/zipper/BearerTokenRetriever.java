package online.kheops.zipper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class BearerTokenRetriever {
    private final Client client;
    private final URI tokenURI;
    private final AccessToken accessToken;

    private static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
    }

    public static final class Builder {
        private Client client;
        private URI authorizationURI;
        private AccessToken accessToken;

        public Builder(){}

        Builder client(Client client) {
            this.client = client;
            return this;
        }
        Builder authorizationURI(URI authorizationURI) {
            this.authorizationURI = authorizationURI;
            return this;
        }

        Builder accessToken(AccessToken accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        BearerTokenRetriever build() {
            return new BearerTokenRetriever(this);
        }
    }

    private BearerTokenRetriever(Builder builder) {
        client = Objects.requireNonNull(builder.client, "client must not be null");
        accessToken = Objects.requireNonNull(builder.accessToken, "client must not be null");
        URI authorizationURI = Objects.requireNonNull(builder.authorizationURI, "authorizationURI must not be null");
        tokenURI = UriBuilder.fromUri(authorizationURI).path("/token").build();
    }

    public Future<BearerToken> get(Instance instance, InvocationCallback<BearerToken> callback) {
        final Form form = getForm().param("scope", "StudyInstanceUID=" + instance.getStudyInstanceUID() + " SeriesInstanceUID=" + instance.getSeriesInstanceUID());

        Future<TokenResponse> tokenFuture = client.target(tokenURI).request("application/json").async().post(Entity.form(form), new InvocationCallback<TokenResponse>() {

            @Override
            public void completed(TokenResponse tokenResponse) {
                callback.completed(BearerToken.newInstance(tokenResponse.accessToken));
            }

            @Override
            public void failed(Throwable throwable) {
                callback.failed(throwable);
            }
        });

        return new Future<BearerToken>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return tokenFuture.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return tokenFuture.isCancelled();
            }

            @Override
            public boolean isDone() {
                return tokenFuture.isDone();
            }

            @Override
            public BearerToken get() throws InterruptedException, ExecutionException {
                return BearerToken.newInstance(tokenFuture.get().accessToken);
            }

            @Override
            public BearerToken get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return BearerToken.newInstance(tokenFuture.get(timeout, unit).accessToken);
            }
        };
    }

    private Form getForm() {
        return new Form().param("assertion", accessToken.getToken()).param("grant_type", accessToken.getTypeUrn());
    }
}
