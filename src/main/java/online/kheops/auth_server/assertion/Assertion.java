package online.kheops.auth_server.assertion;

import online.kheops.auth_server.entity.Capability;

import java.util.Optional;

public interface Assertion {
    String getUsername();
    String getEmail();
    boolean hasCapabilityAccess();
    default Optional<Capability> getCapability() {
        return Optional.empty();
    }
}