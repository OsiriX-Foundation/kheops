package online.kheops.auth_server.assertion.builder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.assertion.GoogleJWTAssertion;
import online.kheops.auth_server.assertion.assertion.SuperuserJWTAssertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;

public class JWTAssertionBuilder implements AssertionBuilder {
    private static final String KHEOPS_ISSUER = "authorization.kheops.online";
    private static final String GOOGLE_ISSUER = "accounts.google.com";

    private final String superuserSecret;

    public JWTAssertionBuilder(String superuserSecret) {
        this.superuserSecret = superuserSecret;
    }

    @Override
    public Assertion build(String assertionToken) throws BadAssertionException {
        final String issuer;

        try {
            issuer = JWT.decode(assertionToken).getIssuer();
        } catch (JWTDecodeException e) {
            throw new BadAssertionException("Unable to decode JWT", e);
        }

        if (issuer == null) {
            throw new BadAssertionException("JWT has no issuer");
        }

        switch (issuer) {
            case KHEOPS_ISSUER:
                SuperuserJWTAssertion superuserJWTAssertion = new SuperuserJWTAssertion();
                superuserJWTAssertion.setSuperuserSecret(superuserSecret);
                superuserJWTAssertion.setAssertionToken(assertionToken);
                return superuserJWTAssertion;
            case GOOGLE_ISSUER:
                GoogleJWTAssertion googleJWTAssertion = new GoogleJWTAssertion();
                googleJWTAssertion.setAssertionToken(assertionToken);
                return googleJWTAssertion;
            default:
                throw new BadAssertionException("Unknown JWT issuer");
        }
    }
}
