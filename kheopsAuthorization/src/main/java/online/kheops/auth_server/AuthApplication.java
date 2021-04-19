package online.kheops.auth_server;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/*")
public class AuthApplication extends ResourceConfig {

    public AuthApplication() {
        register(new ApplicationBinder());
        property("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature,online.kheops.auth_server.filter.CacheFilterFactory");
        packages(true, "online.kheops.auth_server");
    }
}