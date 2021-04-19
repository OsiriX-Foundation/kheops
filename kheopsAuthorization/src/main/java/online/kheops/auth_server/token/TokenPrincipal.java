package online.kheops.auth_server.token;

import java.security.Principal;

public interface TokenPrincipal extends Principal {
    TokenClientKind getClientKind();
}
