package online.kheops.auth_server.accesstoken;

import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;

import javax.servlet.ServletContext;

public interface AccessToken {

    enum TokenType  {KEYCLOAK_TOKEN, CAPABILITY_TOKEN, SUPER_USER_TOKEN, PEP_TOKEN, VIEWER_TOKEN, REPORT_PROVIDER_TOKEN}

    String getSub();
    TokenType getTokenType();
    default String getScope() {
        return "read write";
    }

    KheopsPrincipalInterface newPrincipal(ServletContext servletContext, User user);
}
