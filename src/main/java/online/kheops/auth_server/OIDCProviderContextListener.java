package online.kheops.auth_server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class OIDCProviderContextListener implements ServletContextListener {

    private static ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        servletContext = sce.getServletContext();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        servletContext = null;
    }

    private static void verifyState() {
        if (servletContext == null) {
            throw new IllegalStateException("Getting parameters before the context is initialized");
        }
    }

    private static String getOIDCProvider() {
        verifyState();
        return servletContext.getInitParameter("online.kheops.oidc.provider");
    }

    public static URI getOIDCConfigurationURI() {
        return UriBuilder.fromUri(getOIDCProvider() + "/.well-known/openid-configuration").build();
    }

    public static String getOIDCConfigurationString() {
        return getOIDCProvider() + "/.well-known/openid-configuration";
    }
}
