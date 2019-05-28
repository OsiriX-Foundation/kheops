package online.kheops.auth_server.util;

import java.security.Principal;
import java.util.Objects;

public interface TokenPrincipal extends Principal {
    TokenClientKind getClientKind();
}
