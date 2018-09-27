package online.kheops.proxy;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/capabilities/*")
public class ProxyApplication extends Application {

    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(STOWResource.class);
        return set;
    }

    public Set<Object> getSingletons() {
        Set<Object> set = new HashSet<>();
        set.add(new JSONAttributesWriter());
        set.add(new XMLAttributesWriter());
        set.add(new CORSFilter());
        return set;
    }
}
