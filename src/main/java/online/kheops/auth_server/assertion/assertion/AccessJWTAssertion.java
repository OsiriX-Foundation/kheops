package online.kheops.auth_server.assertion.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;
import online.kheops.auth_server.entity.User;

import javax.servlet.ServletContext;


public class AccessJWTAssertion implements Assertion {

    private String username;
    private String email;
    private Boolean capabilityAccess;


    public void setAssertionToken(String assertionToken, ServletContext context) throws BadAssertionException{

        final DecodedJWT jwt;

        try {
            final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecret");
            final Algorithm kheopsAlgorithmHMAC = Algorithm.HMAC256(authSecret);
            JWTVerifier verifier = JWT.require(kheopsAlgorithmHMAC)
                    .withIssuer("auth.kheops.online")
                    .build();
            jwt = verifier.verify(assertionToken);

            Claim capabilityClaim = jwt.getClaim("capability");
            if (capabilityClaim.asBoolean() != null) {
                capabilityAccess = capabilityClaim.asBoolean();
            } else {
                capabilityAccess = false;
            }
        } catch (JWTVerificationException e) {
            throw new BadAssertionException("Verification of the access token failed", e);
        } catch (Exception e) {
            throw new BadAssertionException("");
        }

        this.username = jwt.getSubject();
        this.email = User.findByUsername(this.username).getGoogleEmail();
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
    public Boolean getCapabilityAccess() {
        return capabilityAccess;
    }

}
