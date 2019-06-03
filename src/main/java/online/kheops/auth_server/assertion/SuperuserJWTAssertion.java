package online.kheops.auth_server.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

final class SuperuserJWTAssertion implements Assertion {
    private final String sub;

    static class Builder {
        private final String superuserSecret;

        private Builder(String superuserSecret) {
            this.superuserSecret = Objects.requireNonNull(superuserSecret);
        }

        SuperuserJWTAssertion build(String assertionToken) throws BadAssertionException {
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
                throw new BadAssertionException("Assertion verification failed.", e);
            }

            if (jwt.getSubject() == null) {
                throw new BadAssertionException("Missing sub claim in token.");
            }

            return new SuperuserJWTAssertion(jwt.getSubject());
        }
    }

    static Builder getBuilder(String superuserSecret) {
        return new Builder(superuserSecret);
    }

    private SuperuserJWTAssertion(String sub) {
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
