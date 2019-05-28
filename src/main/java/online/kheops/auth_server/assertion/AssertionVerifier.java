package online.kheops.auth_server.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class AssertionVerifier {

    private static final Logger LOG = Logger.getLogger(AssertionVerifier.class.getName());


    private static String superuserSecret;
    private static String authorizationSecret;

    // this method needs to be called during app initialization
    public static void setSecrets(String newSuperuserSecret, String newAuthorizationSecret) {
        superuserSecret = newSuperuserSecret;
        authorizationSecret = newAuthorizationSecret;
    }

    private static final String JWT_BEARER_URN = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String CAPABILITY_URN = "urn:x-kheops:params:oauth:grant-type:capability";
    private static final String VIEWER_URN = "urn:x-kheops:params:oauth:grant-type:viewer";
    private static final String UNKNOWN_BEARER_URN = "urn:x-kheops:params:oauth:grant-type:unknown-bearer";

    private enum TokenType {
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
        VIEWER(VIEWER_URN) {
            AssertionBuilder getAssertionBuilder() {
                if (authorizationSecret == null) {
                    throw new IllegalStateException("authorization secret was never set");
                }
                return new ViewerAssertionBuilder(authorizationSecret);
            }
        },
        UNKNOWN_BEARER(UNKNOWN_BEARER_URN) {
            AssertionBuilder getAssertionBuilder() {
                return assertionToken -> {
                    List<BadAssertionException> exceptionList = new ArrayList<>(3);
                    try {
                        return JWT_BEARER.getAssertionBuilder().build(assertionToken);
                    } catch (BadAssertionException e) {
                        exceptionList.add(e);
                    }
                    try {
                        return CAPABILITY.getAssertionBuilder().build(assertionToken);
                    } catch (BadAssertionException e) {
                        exceptionList.add(e);
                    }
                    try {
                        return VIEWER.getAssertionBuilder().build(assertionToken);
                    } catch (BadAssertionException e) {
                        exceptionList.add(e);
                    }

                    final StringBuilder messageBuilder = new StringBuilder("Unable to verify assertion because");
                    exceptionList.forEach(e -> messageBuilder.append(", ").append(e.getMessage()));

                    final BadAssertionException badAssertionException = new BadAssertionException(messageBuilder.toString());
                    exceptionList.forEach(badAssertionException::addSuppressed);

                    throw badAssertionException;
                };
            }
        };

        private final String urn;

        TokenType(String urn) {
            this.urn = urn;
        }

        static TokenType valueOfUrn(String urn) throws UnknownGrantTypeException {
            Objects.requireNonNull(urn);
            for (TokenType tokenType : TokenType.values()) {
                if (tokenType.getUrn().equals(urn)) {
                    return tokenType;
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
        return TokenType.valueOfUrn(grantType).getAssertionBuilder().build(assertionToken);
    }

    public static Assertion createAssertion(String bearerToken) throws BadAssertionException {
        try {
            return createAssertion(bearerToken, UNKNOWN_BEARER_URN);
        } catch (UnknownGrantTypeException e) {
            throw new RuntimeException("Unknown grant type", e);
        }
    }

}

