package online.kheops.proxy.tokens;

import online.kheops.proxy.id.SeriesID;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_PASSWORD;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_USERNAME;

@SuppressWarnings("WeakerAccess")
public class AccessToken {
    private static final Logger LOG = Logger.getLogger(AccessToken.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient().register(HttpAuthenticationFeature.basicBuilder().build());

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    private String token;

    @SuppressWarnings("unused")
    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        String expiresIn;
    }

    public static class AccessTokenBuilder {
        private String clientId;
        private String clientSecret;
        private String capability;
        private SeriesID seriesID;
        private String headerXForwardedFor = null;
        URI authorizationServerRoot;

        private AccessTokenBuilder(URI authorizationServerRoot) {
            this.authorizationServerRoot = authorizationServerRoot;
        }

        public AccessTokenBuilder withCapability(String capability) {
            this.capability = Objects.requireNonNull(capability);
            return this;
        }

        public AccessTokenBuilder withClientId(String clientId) {
            this.clientId = Objects.requireNonNull(clientId);
            return this;
        }

        public AccessTokenBuilder withClientSecret(String clientSecret) {
            this.clientSecret = Objects.requireNonNull(clientSecret);
            return this;
        }

        public AccessTokenBuilder xForwardedFor(String headerXForwardedFor) {
            this.headerXForwardedFor = headerXForwardedFor;
            return this;
        }

        public AccessTokenBuilder withSeriesID(SeriesID seriesID) {
            this.seriesID = Objects.requireNonNull(seriesID);
            return this;
        }

        public AccessToken build() throws AccessTokenException {
            if (capability == null) {
                throw new IllegalStateException("Capability is not set");
            }
            if (clientId == null) {
                throw new IllegalStateException("clientId is not set");
            }
            if (clientSecret == null) {
                throw new IllegalStateException("clientSecret is not set");
            }
            if (seriesID == null) {
                throw new IllegalStateException("seriesID is not set");
            }

            final Form form = new Form()
                    .param("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                    .param("subject_token", capability)
                    .param("subject_token_type", "urn:ietf:params:oauth:token-type:access_token")
                    .param("scope", "pep")
                    .param("studyUID", seriesID.getStudyUID())
                    .param("seriesUID", seriesID.getSeriesUID());

            URI uri = UriBuilder.fromUri(authorizationServerRoot).path("token").build();

            final TokenResponse tokenResponse;
            try {
                tokenResponse = CLIENT.target(uri)
                        .request(APPLICATION_JSON_TYPE)
                        .property(HTTP_AUTHENTICATION_USERNAME, clientId)
                        .property(HTTP_AUTHENTICATION_PASSWORD, clientSecret)
                        .header(HEADER_X_FORWARDED_FOR, headerXForwardedFor)
                        .post(Entity.form(form), TokenResponse.class);
            } catch (ProcessingException | WebApplicationException e) {
                throw new AccessTokenException("Unable to get a request token for the capability URL", e);
            }

            LOG.info("finished getting the access token");
            return new AccessToken(tokenResponse.accessToken);
        }
    }

    private AccessToken(String token) {
        this.token = token;
    }

    public static AccessTokenBuilder createBuilder(URI authorizationServerRoot) {
        return new AccessTokenBuilder(authorizationServerRoot);
    }

    public String getHeaderValue() {
        return "Bearer " + getToken();
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token;
    }
}
