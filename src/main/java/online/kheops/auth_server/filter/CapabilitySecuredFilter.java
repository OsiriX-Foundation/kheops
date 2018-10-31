package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.CapabilitySecured;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@CapabilitySecured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class CapabilitySecuredFilter implements ContainerRequestFilter {


    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!requestContext.getSecurityContext().isUserInRole("capability")) {
            throw new NotAuthorizedException("The bearer token not valid for access to capability resources");
        }
    }
}
