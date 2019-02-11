package online.kheops.zipper.bearertoken;

import online.kheops.zipper.instance.Instance;
import online.kheops.zipper.accesstoken.AccessToken;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static online.kheops.zipper.instance.Instance.SERIES_INSTANCE_UID;
import static online.kheops.zipper.instance.Instance.STUDY_INSTANCE_UID;

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

        public Builder client(Client client) {
            this.client = client;
            return this;
        }
        public Builder authorizationURI(URI authorizationURI) {
            this.authorizationURI = authorizationURI;
            return this;
        }

        public Builder accessToken(AccessToken accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public BearerTokenRetriever build() {
            return new BearerTokenRetriever(this);
        }
    }

    private BearerTokenRetriever(Builder builder) {
        client = Objects.requireNonNull(builder.client, "client");
        accessToken = Objects.requireNonNull(builder.accessToken, "accessToken");
        URI authorizationURI = Objects.requireNonNull(builder.authorizationURI, "authorizationURI");
        tokenURI = UriBuilder.fromUri(authorizationURI).path("/token").build();
    }

    public void get(Instance instance, InvocationCallback<BearerToken> callback) {
        final Form form = getInstanceForm(instance);

        client.target(tokenURI).request(APPLICATION_JSON_TYPE).async().post(Entity.form(form), new InvocationCallback<TokenResponse>() {

            @Override
            public void completed(TokenResponse tokenResponse) {
                callback.completed(BearerToken.newInstance(tokenResponse.accessToken));
            }

            @Override
            public void failed(Throwable throwable) {
                callback.failed(new BearerTokenRetrievalException("Unable to retrieve bearer accesstoken", throwable));
            }
        });
    }

    private Form getForm() {
        return new Form().param("assertion", accessToken.toString()).param("grant_type", AccessToken.UNKNOWN_TOKEN_URN);
    }

    private Form getInstanceForm(Instance instance) {
        return getForm().param("scope", "pep")
                .param("study_instance_uid", instance.getStudyInstanceUID())
                .param("series_instance_uid", instance.getSeriesInstanceUID());
    }
}
