package online.kheops.auth_server.assertion;

import java.util.Objects;

public abstract class AssertionVerifier {
    private static final String JWT_BEARER_URN = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String CAPABILITY_URN = "urn:x-kheops:params:oauth:grant-type:capability";

    private enum GrantType {
        JWTBearer(JWT_BEARER_URN) {
            AssertionBuilder getAssertionBuilder() {
                return GrantType.getJWTBearerAssertionBuilder();
            }
        },
        Capability(CAPABILITY_URN) {
            AssertionBuilder getAssertionBuilder() {
                return GrantType.getCapabilityAssertionBuilder();
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

        private static AssertionBuilder getCapabilityAssertionBuilder() {
            return new CapabilityAssertionBuilder();
        }

        private static AssertionBuilder getJWTBearerAssertionBuilder() {
            if (superuserSecret == null) {
                throw new IllegalStateException("superuser secret was never set");
            }
            if (authorizationSecret == null) {
                throw new IllegalStateException("authorization secret was never set");
            }
            return new JWTAssertionBuilder(superuserSecret, authorizationSecret);
        }
    }

    private static String superuserSecret;
    private static String authorizationSecret;

    private AssertionVerifier() {}

    public static void setSecrets(String newSuperuserSecret, String newAuthorizationSecret) {
        superuserSecret = newSuperuserSecret;
        authorizationSecret = newAuthorizationSecret;
    }

    public static Assertion createAssertion(String assertionToken, String grantType) throws UnknownGrantTypeException, BadAssertionException {
        return GrantType.valueOfUrn(grantType).getAssertionBuilder().build(assertionToken);
    }

    public static Assertion createAssertion(String bearerToken) throws BadAssertionException {
        for (GrantType grantType: GrantType.values()) {
            try {
                return createAssertion(bearerToken, grantType.getUrn());
            } catch (UnknownGrantTypeException e) {
                throw new IllegalStateException("Should not have a bad grant type");
            } catch (BadAssertionException e) {
                /* It's ok if this assertion couldn't be verified, we'll just try another type */
            }
        }
        throw new BadAssertionException("Bad bearer token");
    }

}

