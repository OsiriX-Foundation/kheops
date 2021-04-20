package online.kheops.auth_server.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import static online.kheops.auth_server.util.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;

@Provider
public class ExposeHeadersFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)  {
        final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        if (headers.containsKey(X_TOTAL_COUNT)) {
            headers.add(ACCESS_CONTROL_EXPOSE_HEADERS, X_TOTAL_COUNT);
        }
    }
}
