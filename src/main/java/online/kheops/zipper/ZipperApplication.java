package online.kheops.zipper;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/*")
public final class ZipperApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return super.getClasses();
    }


}
