package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

final class SuperuserJWTAccessToken implements AccessToken {
    private final String sub;

    static class Builder {
        private final String superuserSecret;

        private Builder(String superuserSecret) {
            this.superuserSecret = Objects.requireNonNull(superuserSecret);
        }

        SuperuserJWTAccessToken build(String assertionToken) throws BadAccessTokenException {
            Objects.requireNonNull(assertionToken);

            final Algorithm algorithm;
            try {
                algorithm = Algorithm.HMAC256(superuserSecret);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("superuserSecret is not a valid HMAC256 secret", e);
            }
            final DecodedJWT jwt;
            try {
                jwt = JWT.require(algorithm)
                        .withIssuer("authorization.kheops.online")
                        .build()
                        .verify(assertionToken);
            } catch (JWTVerificationException e) {
                throw new BadAccessTokenException("AccessToken verification failed.", e);
            }

            if (jwt.getSubject() == null) {
                throw new BadAccessTokenException("Missing sub claim in token.");
            }

            return new SuperuserJWTAccessToken(jwt.getSubject());
        }
    }

    static Builder getBuilder(String superuserSecret) {
        return new Builder(superuserSecret);
    }

    private SuperuserJWTAccessToken(String sub) {
        this.sub = Objects.requireNonNull(sub);
    }

    @Override
    public String getSub() {
        return sub;
    }

    @Override
    public boolean hasCapabilityAccess() {
        return true;
    }

    @Override
    public TokenType getTokenType() { return TokenType.SUPER_USER_TOKEN; }
}
