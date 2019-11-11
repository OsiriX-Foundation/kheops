package online.kheops.auth_server.filter;

import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.annotation.Priority;
import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.Priorities.USER;


@Provider
@Priority(USER)
public class XForwardedForFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {

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
            log.ip(ip);
        }
    }
}
