package online.kheops.auth_server.filter;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.annotation.AlbumPermissionSecured2;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@AlbumPermissionSecured2("blabl")
@Provider
@Priority(Priorities.USER)
public class AlbumPermissionSecuredFilterXXX implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(AlbumPermissionSecuredFilterXXX.class.getName());


    @Override
    public void filter(ContainerRequestContext requestContext) {
        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)requestContext.getSecurityContext().getUserPrincipal());
        Annotation[] a =  this.getClass().getAnnotations();

        if (!kheopsPrincipal.hasUserAccess()) {
            requestContext.abortWith(Response.status(FORBIDDEN).build());
        }
    }


}
