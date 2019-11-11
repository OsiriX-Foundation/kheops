package online.kheops.auth_server.filter;

import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;


@Provider
public class XForwardedForFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        KheopsPrincipal kheopsPrincipal = (KheopsPrincipal) requestContext.getSecurityContext().getUserPrincipal();
        KheopsLogBuilder log = kheopsPrincipal.getKheopsLogBuilder();
        if (requestContext.getHeaders().containsKey("X-Forwarded-For")) {
            final String ip = requestContext.getHeaders().get("X-Forwarded-For").get(0);
            log.ip(ip).log2();
        }
    }
}
