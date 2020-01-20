package online.kheops.auth_server.filter;

import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.Priorities.USER;
import static online.kheops.auth_server.util.HttpHeaders.X_LINK_AUTHORIZATION;


@Provider
@Priority(USER)
public class XLinkAuthorizationFilter implements ContainerRequestFilter {

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
        if (requestContext.getHeaders().containsKey(X_LINK_AUTHORIZATION)) {
            final boolean link = Boolean.parseBoolean(requestContext.getHeaders().get(X_LINK_AUTHORIZATION).get(0));
            kheopsPrincipal.setLink(link);
            log.link(link);
        } else {
            log.link(false);
        }
    }
}
