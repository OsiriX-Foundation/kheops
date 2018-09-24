package online.kheops.auth_server.assertion;

import java.util.Objects;

public abstract class AssertionVerifier {
    private static String superuserSecret;
    private static String authorizationSecret;

    // this method needs to be called during app initialization
    public static void setSecrets(String newSuperuserSecret, String newAuthorizationSecret) {
        superuserSecret = newSuperuserSecret;
        authorizationSecret = newAuthorizationSecret;
    }

    private static final String JWT_BEARER_URN = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String CAPABILITY_URN = "urn:x-kheops:params:oauth:grant-type:capability";
    private static final String UNKNOWN_BEARER_URN = "urn:x-kheops:params:oauth:grant-type:unknown-bearer";

    private enum GrantType {
        JWT_BEARER(JWT_BEARER_URN) {
            AssertionBuilder getAssertionBuilder() {
                if (superuserSecret == null) {
                    throw new IllegalStateException("superuser secret was never set");
                }
                if (authorizationSecret == null) {
                    throw new IllegalStateException("authorization secret was never set");
                }
                return new JWTAssertionBuilder(superuserSecret, authorizationSecret);
            }
        },
        CAPABILITY(CAPABILITY_URN) {
            AssertionBuilder getAssertionBuilder() {
                return new CapabilityAssertionBuilder();
            }
        },
        UNKNOWN_BEARER(UNKNOWN_BEARER_URN) {
            AssertionBuilder getAssertionBuilder() {
                return assertionToken -> {
                    try {
                        return JWT_BEARER.getAssertionBuilder().build(assertionToken);
                    } catch (BadAssertionException e) { /* empty */ }
                    try {
                        return CAPABILITY.getAssertionBuilder().build(assertionToken);
                    } catch (BadAssertionException e) { /* empty */ }

                    throw new BadAssertionException("Bad bearer token");
                };
            }
        };

        private final String urn;

        GrantType(String urn) {
            this.urn = urn;
        }

        static GrantType valueOfUrn(String urn) throws UnknownGrantTypeException {
            Objects.requireNonNull(urn);
            for (GrantType grantType: GrantType.values()) {
                if (grantType.getUrn().equals(urn)) {
                    return grantType;
                }
            }
            throw new UnknownGrantTypeException("Unknown grant type: " + urn);
        }

        String getUrn() {
            return urn;
        }

        abstract AssertionBuilder getAssertionBuilder();
    }

    private AssertionVerifier() {}

    public static Assertion createAssertion(String assertionToken, String grantType) throws UnknownGrantTypeException, BadAssertionException {
        return GrantType.valueOfUrn(grantType).getAssertionBuilder().build(assertionToken);
    }

    public static Assertion createAssertion(String bearerToken) throws BadAssertionException {
        try {
            return createAssertion(bearerToken, UNKNOWN_BEARER_URN);
        } catch (UnknownGrantTypeException e) {
            throw new RuntimeException("Unknown grant type", e);
        }
    }

}

