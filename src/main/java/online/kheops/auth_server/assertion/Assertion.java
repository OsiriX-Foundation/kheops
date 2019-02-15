package online.kheops.auth_server.assertion;

import online.kheops.auth_server.entity.Capability;

import javax.json.JsonObject;
import java.util.Optional;

public interface Assertion {

    enum TokenType  {KEYCLOAK_TOKEN, CAPABILITY_TOKEN, SUPER_USER_TOKEN, PEP_TOKEN, VIEWER_TOKEN, GOOGLE_TOKEN}

    String getEmail();
    String getSub();
    TokenType getTokenType();
    boolean hasCapabilityAccess();
    default Optional<Capability> getCapability() {
        return Optional.empty();
    }
    default Optional<JsonObject> getViewer() { return Optional.empty(); }
}
