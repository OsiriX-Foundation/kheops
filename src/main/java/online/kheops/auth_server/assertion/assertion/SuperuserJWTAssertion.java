package online.kheops.auth_server.assertion.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;

import java.io.UnsupportedEncodingException;

public class SuperuserJWTAssertion implements Assertion {
    private String superuserSecret;

    private String username;
    private String email;

    public void setSuperuserSecret(String superuserSecret) {
        this.superuserSecret = superuserSecret;
    }

    public void setAssertionToken(String assertionToken) throws BadAssertionException {
        if (superuserSecret == null) {
            throw new IllegalStateException("superuserSecret was never set");
        }

        final Algorithm algorithm;
        try {
            algorithm = Algorithm.HMAC256(superuserSecret);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("superuserSecret is not a valid HMAC256 secret", e);
        }
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("authorization.kheops.online")
                .build();
        DecodedJWT jwt = verifier.verify(assertionToken);

        if (jwt.getSubject() == null) {
            throw new BadAssertionException("Missing sub claim in token.");
        }

        Claim emailClaim = jwt.getClaim("email");
        if (emailClaim.isNull()) {
            throw new BadAssertionException("Missing email claim in token");
        }

        username = jwt.getSubject();
        email = emailClaim.asString();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }
}
