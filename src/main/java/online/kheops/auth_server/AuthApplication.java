package online.kheops.auth_server;


import online.kheops.auth_server.services.InboxService;
import online.kheops.auth_server.services.TokenService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/*")
public class AuthApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> set = new HashSet<Class<?>>();
        set.add(InboxService.class);
        set.add(TokenService.class);
        return super.getClasses();
    }
}
