package online.kheops.auth_server;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashMap;
import java.util.Map;

@ApplicationPath("/*")
public class AuthApplication extends Application {
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jersey.config.server.provider.classnames",
                "org.glassfish.jersey.media.multipart.MultiPartFeature,online.kheops.auth_server.filter.CacheFilterFactory");
        return props;
    }

}
