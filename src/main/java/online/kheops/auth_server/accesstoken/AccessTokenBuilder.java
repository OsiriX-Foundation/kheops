package online.kheops.auth_server.accesstoken;

import javax.servlet.ServletContext;

abstract class AccessTokenBuilder {
    private final ServletContext servletContext;

    AccessTokenBuilder() {
        servletContext = null;
    }

    AccessTokenBuilder(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    abstract AccessToken build(String assertionToken) throws AccessTokenVerificationException;

    protected ServletContext getServletContext() {
        if (servletContext == null) {
            throw new IllegalStateException("Can't access the servlet context if it was not set in the constructor");
        }
        return servletContext;
    }
}
