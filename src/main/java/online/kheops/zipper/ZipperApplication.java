package online.kheops.zipper;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;


@ApplicationPath("/*")
public final class ZipperApplication extends ResourceConfig {

    public ZipperApplication() {
        property("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature,online.kheops.zipper.filter.CacheFilterFactory");
        packages(true, "online.kheops.zipper");
    }

}
