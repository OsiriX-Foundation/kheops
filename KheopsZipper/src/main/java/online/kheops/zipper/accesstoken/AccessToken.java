package online.kheops.zipper.accesstoken;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AccessToken {
    private static final Logger LOG = Logger.getLogger(AccessToken.class.getName());

    private final String token;

    private AccessToken(String token) {
        this.token = Objects.requireNonNull(token, "accesstoken");
    }

    private static AccessToken from(String token) {
        return new AccessToken(token);
    }

    public static AccessToken fromAuthorizationHeader(String authorizationHeader) {
        final String token;
        if (authorizationHeader != null) {

            if (authorizationHeader.toUpperCase().startsWith("BASIC ")) {
                final String encodedAuthorization = authorizationHeader.substring(6);

                final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
                String[] split = decoded.split(":");
                if (split.length != 2) {
                    LOG.log(Level.WARNING, "Basic authentication does not have a username and password");
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
                LOG.log(Level.WARNING, "Empty authorization accesstoken");
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } else {
            LOG.log(Level.WARNING, "Missing authorization header");
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        return AccessToken.from(token);
    }

    @Override
    public String toString() {
        return token;
    }
}
