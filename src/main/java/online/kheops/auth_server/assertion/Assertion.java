package online.kheops.auth_server.assertion;

public interface Assertion {
    String getUsername();
    String getEmail();
    boolean getCapabilityAccess();

    default boolean isCapabilityAssertion() {
        return false;
    }
}
