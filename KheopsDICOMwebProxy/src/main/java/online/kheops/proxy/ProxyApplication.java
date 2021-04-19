package online.kheops.proxy;

import online.kheops.proxy.filter.CORSFilter;
import online.kheops.proxy.filter.FixTypeQuoteFilter;
import online.kheops.proxy.filter.IncludefieldFilter;
import online.kheops.proxy.marshaller.*;
import online.kheops.proxy.multipart.MultipartStreamingProvider;
import online.kheops.proxy.ohif.OHIFMetadataResource;
import online.kheops.proxy.stow.resource.Resource;
import online.kheops.proxy.wadors.*;
import online.kheops.proxy.wadouri.WadoUriResource;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/*")
public class ProxyApplication extends Application {

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
        set.add(MultiPartFeature.class);
        set.add(JSONAttributesProvider.class);
        set.add(JSONAttributesListProvider.class);
        set.add(XMLAttributesProvider.class);
        set.add(XMLAttributesListProvider.class);
        set.add(MultipartStreamingProvider.class);
        set.add(StreamingAttributesProvider.class);
        set.add(CORSFilter.class);
        set.add(IncludefieldFilter.class);

        return set;
    }
}
