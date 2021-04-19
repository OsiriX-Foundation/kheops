package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.CapabilitySecured;
import online.kheops.auth_server.util.Consts;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@CapabilitySecured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class CapabilitySecuredFilter implements ContainerRequestFilter {


    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!requestContext.getSecurityContext().isUserInRole(Consts.USER_IN_ROLE.CAPABILITY)) {
            throw new NotAuthorizedException("The bearer token not valid for access to capability resources");
            //TODO NotAuthorized => Forbidden
        }
    }
}
