package online.kheops.auth_server;

import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashMap;
import java.util.Map;

@ApplicationPath("/*")
public class AuthApplication extends Application {
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ServerProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 0);
        return props;
    }
}
