package online.kheops.proxy;

import online.kheops.proxy.filter.CORSFilter;
import online.kheops.proxy.marshaller.*;
import online.kheops.proxy.multipart.MultipartStreamingProvider;
import online.kheops.proxy.ohif.OHIFMetadataResource;
import online.kheops.proxy.stow.resource.Resource;
import online.kheops.proxy.wadors.*;
import online.kheops.proxy.wadouri.WadoUriResource;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ApplicationPath("/*")
public class ProxyApplication extends Application {

    @Context
    Providers providers;

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(Resource.class);
        set.add(WadoUriResource.class);
        set.add(WadoRsMetadata.class);
        set.add(WadoRsResource.class);
        set.add(WadoRSStudy.class);
        set.add(WadoRSStudyInstance.class);
        set.add(WadoRSSeriesInstances.class);
        set.add(OHIFMetadataResource.class);
        return set;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> set = new HashSet<>();
        set.add(new JSONAttributesProvider());
        set.add(new JSONAttributesListProvider());
        set.add(new XMLAttributesProvider());
        set.add(new XMLAttributesListProvider(providers));
        set.add(new MultipartStreamingProvider(providers));
        set.add(new StreamingAttributesProvider(providers));
        set.add(new CORSFilter());
        return set;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ServerProperties.PROVIDER_CLASSNAMES, "org.glassfish.jersey.media.multipart.MultiPartFeature");

        return props;
    }
}
