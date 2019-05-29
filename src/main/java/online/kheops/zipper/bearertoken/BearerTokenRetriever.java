package online.kheops.zipper.bearertoken;

import online.kheops.zipper.instance.Instance;
import online.kheops.zipper.accesstoken.AccessToken;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_PASSWORD;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_USERNAME;

public final class BearerTokenRetriever {
    private static final Client CLIENT = ClientBuilder.newClient().register(HttpAuthenticationFeature.basicBuilder().build());

    private final ServletContext servletContext;
    private final URI tokenURI;
    private final AccessToken accessToken;

    private static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
    }

    public static final class Builder {
        private final ServletContext servletContext;
        private URI authorizationURI;
        private AccessToken accessToken;

        public Builder(ServletContext servletContext) {
            this.servletContext = Objects.requireNonNull(servletContext);
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
        servletContext = builder.servletContext;
        accessToken = Objects.requireNonNull(builder.accessToken, "accessToken");
        URI authorizationURI = Objects.requireNonNull(builder.authorizationURI, "authorizationURI");
        tokenURI = UriBuilder.fromUri(authorizationURI).path("/token").build();
    }

    public void get(Instance instance, InvocationCallback<BearerToken> callback) {
        final Form form = getInstanceForm(instance);

        CLIENT.target(tokenURI)
                .request(APPLICATION_JSON_TYPE)
                .property(HTTP_AUTHENTICATION_USERNAME, servletContext.getInitParameter("online.kheops.client.zipperclientid"))
                .property(HTTP_AUTHENTICATION_PASSWORD, servletContext.getInitParameter("online.kheops.client.zippersecret"))
                .async().
                post(Entity.form(form), new InvocationCallback<TokenResponse>() {

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
        return new Form()
                .param("subject_token", accessToken.toString())
                .param("subject_token_type", "urn:ietf:params:oauth:token-type:access_token")
                .param("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
    }

    private Form getInstanceForm(Instance instance) {
        return getForm().param("scope", "pep")
                .param("studyUID", instance.getStudyInstanceUID())
                .param("seriesUID", instance.getSeriesInstanceUID());
    }
}
