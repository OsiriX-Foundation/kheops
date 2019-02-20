package online.kheops.proxy.tokens;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class Introspect {
    private static final Client CLIENT = ClientBuilder.newClient();

    public static class Response {
        @XmlElement(name = "active")
        boolean active;
        @XmlElement(name = "scope")
        String scope;

        public boolean isActive() {
            return active;
        }

        public boolean isValidForScope(String requestedScope) {
            if (!active) {
                return false;
            }

            final String[] words = scope.split("\\s+", 40);
            for (String word : words) {
                if (word.equals(requestedScope)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class Introspector {
        private final WebTarget webTarget;

        private Introspector(URI endpoint) {
            webTarget = CLIENT.target(Objects.requireNonNull(endpoint));
        }

        public Response token(String token) throws AccessTokenException {
            final Form form = new Form()
                    .param("assertion", token)
                    .param("grant_type", "urn:x-kheops:params:oauth:grant-type:unknown-bearer");

            try {
                return webTarget.request(APPLICATION_JSON_TYPE).post(Entity.form(form), Response.class);
            } catch (ProcessingException | WebApplicationException e) {
                throw new AccessTokenException("Error introspecting", e);
            }
        }
    }

    public static Introspector endpoint(URI endpoint) {
        return new Introspector(endpoint);
    }

}
