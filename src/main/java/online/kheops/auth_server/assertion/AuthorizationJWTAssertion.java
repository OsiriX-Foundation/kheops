package online.kheops.auth_server.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.entity.User;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

final class AuthorizationJWTAssertion implements Assertion {

    private final String username;
    private final String email;
    private final boolean capabilityAccess;

    static final class Builder {
        private final String authorizationSecret;

        private Builder(String authorizationSecret) {
            this.authorizationSecret = Objects.requireNonNull(authorizationSecret);
        }

        AuthorizationJWTAssertion build(String assertionToken) throws BadAssertionException {
            try {
                final DecodedJWT jwt = JWT.require(Algorithm.HMAC256(authorizationSecret))
                        .withIssuer("auth.kheops.online")
                        .build()
                        .verify(assertionToken);

                final User user = User.findByUsername(jwt.getSubject()).orElseThrow(() -> new BadAssertionException("Can't find user"));
                final Boolean capabilityClaim = jwt.getClaim("capability").asBoolean();
                boolean capabilityBoolean = false;
                if (capabilityClaim != null) {
                    capabilityBoolean = capabilityClaim;
                }
                return new AuthorizationJWTAssertion(jwt.getSubject(), user.getGoogleEmail(), capabilityBoolean);

            } catch (JWTVerificationException | UnsupportedEncodingException e) {
                throw new BadAssertionException("Verification of the access token failed", e);
            }
        }
    }

    private AuthorizationJWTAssertion(String username, String email, boolean capabilityAccess) {
        this.username = username;
        this.email = email;
        this.capabilityAccess = capabilityAccess;
    }

    static Builder getBuilder(String authorizationSecret) {
        return new Builder(authorizationSecret);
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
    public boolean hasCapabilityAccess() {
        return capabilityAccess;
    }

}
