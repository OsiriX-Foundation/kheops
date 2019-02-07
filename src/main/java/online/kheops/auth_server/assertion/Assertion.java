package online.kheops.auth_server.assertion;

import online.kheops.auth_server.entity.Capability;

import javax.json.JsonObject;
import java.util.Optional;

public interface Assertion {
    String getEmail();
    String getSub();
    boolean hasCapabilityAccess();
    default Optional<Capability> getCapability() {
        return Optional.empty();
    }
    default Optional<JsonObject> getViewer() { return Optional.empty(); }
}
