package online.kheops.proxy.tokens;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorizationToken {
    private static final Logger LOG = Logger.getLogger(AuthorizationToken.class.getName());

    private final String token;

    private AuthorizationToken(String token) {
        this.token = token;
    }

    public static AuthorizationToken from(String token) {
        return new AuthorizationToken(token);
    }

    public static AuthorizationToken fromAuthorizationHeader(String authorizationHeader) {
        final String token;
        if (authorizationHeader != null) {

            if (authorizationHeader.toUpperCase().startsWith("BASIC ")) {
                final String encodedAuthorization = authorizationHeader.substring(6);

                final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
                String[] split = decoded.split(":");
                if (split.length != 2) {
                    LOG.log(Level.WARNING, "Basic authentication doesn't have a username and password");
                    throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
                }

                token = split[1];
            } else if (authorizationHeader.toUpperCase().startsWith("BEARER ")) {
                token = authorizationHeader.substring(7);
            } else {
                LOG.log(Level.WARNING, "Unknown authorization header");
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
            }

            if (token.length() == 0) {
                LOG.log(Level.WARNING, "Empty authorization token");
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } else {
            LOG.log(Level.WARNING, "Missing authorization header");
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        return AuthorizationToken.from(token);
    }


    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return getToken();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AuthorizationToken && token.equals(((AuthorizationToken) o).getToken());
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }
}
