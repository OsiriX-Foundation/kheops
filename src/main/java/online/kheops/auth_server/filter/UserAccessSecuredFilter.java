package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.util.ErrorResponse;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static online.kheops.auth_server.util.Consts.USER_ACCESS_PRIORITY;
import static online.kheops.auth_server.util.ErrorResponse.Message.AUTHORIZATION_ERROR;

@UserAccessSecured
@Provider
@Priority(USER_ACCESS_PRIORITY)
public class UserAccessSecuredFilter implements ContainerRequestFilter {

    private static final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
            .message(AUTHORIZATION_ERROR)
            .detail("You don't have the permission to do this with this type of token")
            .build();

    @Override
    public void filter(ContainerRequestContext requestContext) {
        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)requestContext.getSecurityContext().getUserPrincipal());

        if (!kheopsPrincipal.hasUserAccess()) {
            requestContext.abortWith(Response.status(FORBIDDEN).entity(errorResponse).build());
        }
    }
}
