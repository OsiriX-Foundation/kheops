package online.kheops.zipper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
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

    public void get(Instance instance, InvocationCallback<BearerToken> callback) {
        final Form form = getInstanceForm(instance);

        client.target(tokenURI).request(MediaType.APPLICATION_JSON_TYPE).async().post(Entity.form(form), new InvocationCallback<TokenResponse>() {

            @Override
            public void completed(TokenResponse tokenResponse) {
                callback.completed(BearerToken.newInstance(tokenResponse.accessToken));
            }

            @Override
            public void failed(Throwable throwable) {
                callback.failed(new BearerTokenRetrievalException("Unable to retrieve bearer token", throwable));
            }
        });
    }

    private Form getForm() {
        return new Form().param("assertion", accessToken.getToken()).param("grant_type", accessToken.getTypeUrn());
    }

    private Form getInstanceForm(Instance instance) {
        return getForm().param("scope", "StudyInstanceUID=" + instance.getStudyInstanceUID() + " SeriesInstanceUID=" + instance.getSeriesInstanceUID());
    }
}
