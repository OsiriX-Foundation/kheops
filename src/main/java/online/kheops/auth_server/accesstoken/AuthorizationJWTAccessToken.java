package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.Users;

import javax.servlet.ServletContext;
import java.io.UnsupportedEncodingException;

final class AuthorizationJWTAccessToken implements AccessToken {

    private final String sub;
    private final boolean capabilityAccess;

    static final class Builder extends AccessTokenBuilder {

        private Builder(ServletContext servletContext) {
            super(servletContext);
        }

        AuthorizationJWTAccessToken build(String assertionToken) throws AccessTokenVerificationException {
            try {
                final DecodedJWT jwt = JWT.require(Algorithm.HMAC256(authorizationSecret()))
                        .withIssuer("auth.kheops.online")
                        .build()
                        .verify(assertionToken);

                try {
                    Users.getOrCreateUser(jwt.getSubject());
                } catch (UserNotFoundException e) {
                    throw new AccessTokenVerificationException("Can't find user");
                }
                final Boolean capabilityClaim = jwt.getClaim("capability").asBoolean();
                boolean capabilityBoolean = false;
                if (capabilityClaim != null) {
                    capabilityBoolean = capabilityClaim;
                }
                return new AuthorizationJWTAccessToken(jwt.getSubject(), capabilityBoolean);

            } catch (JWTVerificationException | UnsupportedEncodingException e) {
                throw new AccessTokenVerificationException("Verification of the access token failed", e);
            }
        }

        private String authorizationSecret() {
            return getServletContext().getInitParameter("online.kheops.auth.hmacsecret");
        }
    }

    private AuthorizationJWTAccessToken(String sub, boolean capabilityAccess) {
        this.sub = sub;
        this.capabilityAccess = capabilityAccess;
    }

    @Override
    public String getSub() {
        return sub;
    }

    @Override
    public boolean hasCapabilityAccess() {
        return capabilityAccess;
    }

    @Override
    public TokenType getTokenType() {
        return TokenType.PEP_TOKEN;
    }
}
