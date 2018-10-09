package online.kheops.proxy;

import online.kheops.proxy.filter.CORSFilter;
import online.kheops.proxy.marshaller.JSONAttributesWriter;
import online.kheops.proxy.marshaller.XMLAttributesWriter;
import online.kheops.proxy.stow.Resource;
import online.kheops.proxy.wadors.WadoRsResource;
import online.kheops.proxy.wadouri.WadoUriResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/*")
public class ProxyApplication extends Application {

    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(Resource.class);
        set.add(WadoUriResource.class);
        set.add(WadoRsResource.class);
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
