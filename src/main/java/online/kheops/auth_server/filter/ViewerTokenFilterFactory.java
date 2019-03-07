package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.ViewerTokenAccess;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static online.kheops.auth_server.util.Consts.USER_IN_ROLE;
import static online.kheops.auth_server.util.Consts.VIEWER_TOKEN_ACCESS_PRIORITY;

@Provider
public class ViewerTokenFilterFactory implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        ViewerTokenAccess vta = resourceInfo.getResourceMethod().getAnnotation(ViewerTokenAccess.class);
        if (vta != null) {
            featureContext.register(new AlbumPermissionFilter(true));
        } else {
            featureContext.register(new AlbumPermissionFilter(false));
        }
    }

    @Priority(VIEWER_TOKEN_ACCESS_PRIORITY)
    private class AlbumPermissionFilter implements ContainerRequestFilter {

        private boolean canAccessWithViewerToken;

        AlbumPermissionFilter(boolean canAccessWithViewerToken) {
            this.canAccessWithViewerToken = canAccessWithViewerToken;
        }

        @Override
        public void filter(ContainerRequestContext requestContext) {
            if(requestContext.getSecurityContext().isUserInRole(USER_IN_ROLE.VIEWER_TOKEN) && !canAccessWithViewerToken) {
                requestContext.abortWith(Response.status(FORBIDDEN).entity("This resource is not available with a viewer token").build());
            }
        }
    }
}
