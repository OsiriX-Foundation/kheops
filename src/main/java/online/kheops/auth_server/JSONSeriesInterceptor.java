package online.kheops.auth_server;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;

@Provider
@Priority(10)
public class JSONSeriesInterceptor implements ReaderInterceptor {
    public JSONSeriesInterceptor() {
        System.out.println("constructor");

    }

    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException {
        if (context.getType().isAssignableFrom(JSONSeries.class)) {
            System.out.println("the right one");
        } else {
            System.out.println("the wrong one");
        }

        return context.proceed();
    }
}
