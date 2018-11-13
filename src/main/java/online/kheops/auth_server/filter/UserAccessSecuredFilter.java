package online.kheops.auth_server.filter;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UserAccessSecured;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@UserAccessSecured
@Provider
@Priority(Priorities.USER)
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
