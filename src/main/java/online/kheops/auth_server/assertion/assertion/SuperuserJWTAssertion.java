package online.kheops.auth_server.assertion.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class SuperuserJWTAssertion implements Assertion {
    private final String username;
    private final String email;

    public static class Builder {
        private final String superuserSecret;

        private Builder(String superuserSecret) {
            this.superuserSecret = Objects.requireNonNull(superuserSecret);
        }

        public SuperuserJWTAssertion build(String assertionToken) throws BadAssertionException {
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

            final Claim emailClaim = jwt.getClaim("email");
            if (emailClaim.isNull()) {
                throw new BadAssertionException("Missing email claim in token");
            }

            return new SuperuserJWTAssertion(jwt.getSubject(), emailClaim.asString());
        }
    }

    public static Builder getBuilder(String superuserSecret) {
        return new Builder(superuserSecret);
    }

    private SuperuserJWTAssertion(String username, String email) {
        this.username = Objects.requireNonNull(username);
        this.email = Objects.requireNonNull(email);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }


    @Override
    public boolean getCapabilityAccess() {
        return true;
    }
}
