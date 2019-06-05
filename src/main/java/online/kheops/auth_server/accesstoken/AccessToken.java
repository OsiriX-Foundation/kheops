package online.kheops.auth_server.accesstoken;

import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.CapabilityPrincipal;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.principal.UserPrincipal;
import online.kheops.auth_server.principal.ViewerPrincipal;

import javax.json.JsonObject;
import javax.servlet.ServletContext;
import java.util.Optional;

public interface AccessToken {

    enum TokenType  {KEYCLOAK_TOKEN, CAPABILITY_TOKEN, SUPER_USER_TOKEN, PEP_TOKEN, VIEWER_TOKEN, REPORT_PROVIDER_TOKEN}

    String getSub();
    TokenType getTokenType();
    boolean hasCapabilityAccess();
    default Optional<Capability> getCapability() {
        return Optional.empty();
    }
    default Optional<JsonObject> getViewer() { return Optional.empty(); }

    default KheopsPrincipalInterface newPrincipal(ServletContext servletContext, User user) {
        Capability capability;
        JsonObject viewer;
        if((capability = getCapability().orElse(null)) != null) {
            return new CapabilityPrincipal(capability, user);
        } else if((viewer = getViewer().orElse(null)) != null) {
            return new ViewerPrincipal(servletContext, viewer);
        } else {
            return new UserPrincipal(user);
        }
    }
}
