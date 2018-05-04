package online.kheops.auth_server;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

@ApplicationPath("/*")
public class AuthApplication extends Application {

    @Context
    ServletContext servletContext;

    @PostConstruct
    public void readInitParams() {
        PersistenceUtils.setUser(servletContext.getInitParameter("online.kheops.jdbc.user"));
        PersistenceUtils.setPassword(servletContext.getInitParameter("online.kheops.jdbc.password"));
        PersistenceUtils.setUrl(servletContext.getInitParameter("online.kheops.jdbc.url"));
    }
}
