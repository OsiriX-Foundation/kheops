package online.kheops.auth_server.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.Users;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

final class AuthorizationJWTAssertion implements Assertion {

    private final String sub;
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

                final User user;
                try {
                    user = Users.getOrCreateUser(jwt.getSubject());
                } catch (UserNotFoundException e) {
                    throw new BadAssertionException("Can't find user");
                }
                final Boolean capabilityClaim = jwt.getClaim("capability").asBoolean();
                boolean capabilityBoolean = false;
                if (capabilityClaim != null) {
                    capabilityBoolean = capabilityClaim;
                }
                return new AuthorizationJWTAssertion(jwt.getSubject(), capabilityBoolean);

            } catch (JWTVerificationException | UnsupportedEncodingException e) {
                throw new BadAssertionException("Verification of the access token failed", e);
            }
        }
    }

    private AuthorizationJWTAssertion(String sub, boolean capabilityAccess) {
        this.sub = sub;
        this.capabilityAccess = capabilityAccess;
    }

    static Builder getBuilder(String authorizationSecret) {
        return new Builder(authorizationSecret);
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
