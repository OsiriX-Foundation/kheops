package online.kheops.auth_server.filter;

import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;


@Provider
public class XForwardedForFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        final KheopsPrincipal kheopsPrincipal;
        final KheopsLogBuilder log;
        try {
            kheopsPrincipal = (KheopsPrincipal) requestContext.getSecurityContext().getUserPrincipal();
            log = kheopsPrincipal.getKheopsLogBuilder();
        } catch(Exception e) {
            return;
        }
        if (requestContext.getHeaders().containsKey("X-Forwarded-For")) {
            final String ip = requestContext.getHeaders().get("X-Forwarded-For").get(0);
            log.ip(ip).log();
        }
    }
}
