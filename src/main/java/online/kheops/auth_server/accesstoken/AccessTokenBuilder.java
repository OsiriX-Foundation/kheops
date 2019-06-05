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

    abstract AccessToken build(String assertionToken) throws BadAccessTokenException;

    protected ServletContext getServletContext() {
        return servletContext;
    }
}
