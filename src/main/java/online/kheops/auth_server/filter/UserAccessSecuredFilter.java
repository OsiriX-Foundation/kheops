package online.kheops.auth_server.filter;

import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.annotation.UserAccessSecured;


import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static online.kheops.auth_server.util.Consts.USER_ACCESS_PRIORITY;

@UserAccessSecured
@Provider
@Priority(USER_ACCESS_PRIORITY)
public class UserAccessSecuredFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(UserAccessSecuredFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)requestContext.getSecurityContext().getUserPrincipal());

        if (!kheopsPrincipal.hasUserAccess()) {
            requestContext.abortWith(Response.status(FORBIDDEN).build());
        }
    }


}
