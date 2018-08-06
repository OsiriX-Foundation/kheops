package online.kheops.proxy;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public class AccessToken {
    private static final Logger LOG = Logger.getLogger(AccessToken.class.getName());
    private static Client client = ClientBuilder.newClient();


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
        private String capability;
        URI authorizationServerRoot;

        private AccessTokenBuilder(URI authorizationServerRoot) {
            this.authorizationServerRoot = authorizationServerRoot;
        }

        public AccessTokenBuilder withCapability(String capability) {
            this.capability = capability;
            return this;
        }

        public AccessToken build() {
            if (capability == null) {
                throw new IllegalStateException("Capability is not set");
            }

            Form form = new Form().param("assertion", capability).param("grant_type", "urn:x-kheops:params:oauth:grant-type:capability").param("return_user", "true");
            URI uri = UriBuilder.fromUri(authorizationServerRoot).path("token").build();

            LOG.info("About to get a token");

            final TokenResponse tokenResponse;
            try {
                tokenResponse = client.target(uri).request("application/json").post(Entity.form(form), TokenResponse.class);
            } catch (ResponseProcessingException e) {
                LOG.log(Level.WARNING,"Unable to obtain a token for capability token", e);
                throw new IllegalStateException("Unable to get a request token for the capability URL", e);
            } catch (Exception e) {
                LOG.log(Level.WARNING,"Other exception Unable to obtain a token for capability token", e);
                throw new IllegalStateException("Unable to get a request token for the capability URL", e);
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

    public String getToken() {
        return token;
    }
}
