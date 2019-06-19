package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.Users;

import javax.servlet.ServletContext;
import java.io.UnsupportedEncodingException;

final class PepAccessToken implements AccessToken {

    private final String sub;

    static final class Builder implements AccessTokenBuilder {
        private final ServletContext servletContext;

        Builder(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public PepAccessToken build(String assertionToken) throws AccessTokenVerificationException {
            try {
                final DecodedJWT jwt = JWT.require(Algorithm.HMAC256(authorizationSecret()))
                        .withIssuer("auth.kheops.online")
                        .withAudience("dicom.kheops.online")
                        .build()
                        .verify(assertionToken);

                try {
                    Users.getOrCreateUser(jwt.getSubject());
                } catch (UserNotFoundException e) {
                    throw new AccessTokenVerificationException("Can't find user");
                }
                return new PepAccessToken(jwt.getSubject());

            } catch (JWTVerificationException | UnsupportedEncodingException e) {
                throw new AccessTokenVerificationException("Verification of the access token failed", e);
            }
        }

        private String authorizationSecret() {
            return servletContext.getInitParameter("online.kheops.auth.hmacsecret");
        }
    }

    private PepAccessToken(String sub) {
        this.sub = sub;
    }

    @Override
    public String getSub() {
        return sub;
    }

    @Override
    public TokenType getTokenType() {
        return TokenType.PEP_TOKEN;
    }

    @Override
    public KheopsPrincipalInterface newPrincipal(ServletContext servletContext, User user) {
        throw new UnsupportedOperationException("Can't make a KheopsPrincipal from a PEP Access Token");
    }
}
