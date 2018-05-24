package online.kheops.auth_server;

public interface Assertion {
    String getUsername();
    String getEmail();

    default boolean isCapabilityAssertion() {
        return false;
    }
}
