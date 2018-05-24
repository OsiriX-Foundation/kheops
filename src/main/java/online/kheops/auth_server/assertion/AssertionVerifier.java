package online.kheops.auth_server.assertion;

import online.kheops.auth_server.assertion.builder.AssertionBuilder;
import online.kheops.auth_server.assertion.builder.CapabilityAssertionBuilder;
import online.kheops.auth_server.assertion.builder.JWTAssertionBuilder;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;
import online.kheops.auth_server.assertion.exceptions.UnknownGrantTypeException;

public abstract class AssertionVerifier {
    private static final String JWT_BEARER_URN = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String CAPABILITY_URN = "urn:x-kheops:params:oauth:grant-type:capability";

    private static String superuserSecret;

    public static void setSuperuserSecret(String newSuperuserSecret) {
        superuserSecret = newSuperuserSecret;
    }

    public static Assertion createAssertion(String assertionToken, String grantType) throws UnknownGrantTypeException, BadAssertionException {
        AssertionBuilder assertionBuilder;

        switch (grantType) {
            case JWT_BEARER_URN:
                if (superuserSecret == null) {
                    throw new IllegalStateException("superuser secret was never set");
                }
                assertionBuilder = new JWTAssertionBuilder(superuserSecret);
                break;
            case CAPABILITY_URN:
                assertionBuilder = new CapabilityAssertionBuilder();
                break;
            default:
                throw new UnknownGrantTypeException("Unknown grant type: " + grantType);
        }

        return assertionBuilder.build(assertionToken);
    }
}

