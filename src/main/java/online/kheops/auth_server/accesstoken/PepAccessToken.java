package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.token.TokenAuthenticationContext;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.Users;

final class PepAccessToken implements AccessToken {

    private final String sub;

    static final class Builder implements AccessTokenBuilder {
        private final TokenAuthenticationContext tokenAuthenticationContext;

        Builder(TokenAuthenticationContext tokenAuthenticationContext) {
            this.tokenAuthenticationContext = tokenAuthenticationContext;
        }

        public PepAccessToken build(String assertionToken, boolean verifySignature) throws AccessTokenVerificationException {
            final DecodedJWT jwt;
            try {
                jwt = JWT.require(Algorithm.HMAC256(authorizationSecret()))
                        .withIssuer("auth.kheops.online")
                        .withAudience("dicom.kheops.online")
                        .acceptLeeway(60)
                        .build()
                        .verify(assertionToken);
            } catch (JWTVerificationException | IllegalArgumentException e) {
                throw new AccessTokenVerificationException("Verification of the access token failed", e);
            }

            try {
                Users.getUser(jwt.getSubject());
            } catch (UserNotFoundException e) {
                throw new AccessTokenVerificationException("Can't find user");
            }

            return new PepAccessToken(jwt.getSubject());
        }

        private String authorizationSecret() {
            return tokenAuthenticationContext.getServletContext().getInitParameter("online.kheops.auth.hmacsecret");
        }
    }

    private PepAccessToken(String sub) {
        this.sub = sub;
    }

    @Override
    public String getSubject() {
        return sub;
    }

    @Override
    public TokenType getTokenType() {
        return TokenType.PEP_TOKEN;
    }

    @Override
    public KheopsPrincipal newPrincipal(TokenAuthenticationContext tokenAuthenticationContext, User user) {
        throw new UnsupportedOperationException("Can't make a KheopsPrincipal from a PEP Access Token");
    }

}
