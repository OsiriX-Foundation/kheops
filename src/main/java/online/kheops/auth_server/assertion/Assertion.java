package online.kheops.auth_server.assertion;

public interface Assertion {
    String getUsername();
    String getEmail();

    default boolean isCapabilityAssertion() {
        return false;
    }
}
