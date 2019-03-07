package online.kheops.auth_server.assertion;

import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.CapabilityPrincipal;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.principal.UserPrincipal;
import online.kheops.auth_server.principal.ViewerPrincipal;

import javax.json.JsonObject;
import java.util.Optional;

public interface Assertion {

    enum TokenType  {KEYCLOAK_TOKEN, CAPABILITY_TOKEN, SUPER_USER_TOKEN, PEP_TOKEN, VIEWER_TOKEN}

    String getEmail();
    String getSub();
    TokenType getTokenType();
    boolean hasCapabilityAccess();
    default Optional<Capability> getCapability() {
        return Optional.empty();
    }
    default Optional<JsonObject> getViewer() { return Optional.empty(); }

    default KheopsPrincipalInterface newPrincipal(User user) {
        Capability capability;
        JsonObject viewer;
        if((capability = getCapability().orElse(null)) != null) {
            return new CapabilityPrincipal(capability, user);
        } else if((viewer = getViewer().orElse(null)) != null) {
            return new ViewerPrincipal(viewer);
        } else {
            return new UserPrincipal(user);
        }
    }
}
