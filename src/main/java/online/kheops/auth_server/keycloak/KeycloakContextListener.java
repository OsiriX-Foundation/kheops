package online.kheops.auth_server.keycloak;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class KeycloakContextListener implements ServletContextListener {

    private static ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        servletContext = sce.getServletContext();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        servletContext = null;
    }

    public static String getKeycloakUri(){
        return servletContext.getInitParameter("online.kheops.keycloak.uri");
    }

    public static String getKeycloakUser(){
        return servletContext.getInitParameter("online.kheops.keycloak.user");
    }

    public static String getKeycloakPassword(){ return servletContext.getInitParameter("online.kheops.keycloak.password"); }

    public static String getKeycloakClientId(){ return servletContext.getInitParameter("online.kheops.keycloak.clientid"); }

    public static String getKeycloakClientSecret(){ return servletContext.getInitParameter("online.kheops.keycloak.clientsecret"); }

    public static String getKeycloakRealms(){ return servletContext.getInitParameter("online.kheops.keycloak.realms"); }
}