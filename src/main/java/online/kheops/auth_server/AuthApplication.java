package online.kheops.auth_server;

import online.kheops.auth_server.resource.*;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/*")
public class AuthApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> set = new HashSet<Class<?>>();
        set.add(InboxResource.class);
        set.add(TokenResource.class);
        return super.getClasses();
    }
}
