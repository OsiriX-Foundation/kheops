package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.user.AlbumUserPermissions;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.ALBUM_PERMISSION_ACCESS_PRIORITY;

@Provider
public class AlbumPermissionFilterFactory implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        AlbumPermissionSecured aps = resourceInfo.getResourceMethod().getAnnotation(AlbumPermissionSecured.class);
        if (aps != null) {
            featureContext.register(new AlbumPermissionFilter(aps.permission(), aps.context()));
        }
        
        AlbumPermissionSecured.List aps2 = resourceInfo.getResourceMethod().getAnnotation(AlbumPermissionSecured.List.class);
        if (aps != null) {
            for (AlbumPermissionSecured a:aps2.value()) {
                featureContext.register(new AlbumPermissionFilter(a.permission(), a.context()));
            }
        }
    }

    @Priority(ALBUM_PERMISSION_ACCESS_PRIORITY)
    private static class AlbumPermissionFilter implements ContainerRequestFilter {
        private final AlbumUserPermissions permission;
        private final AlbumPermissionSecuredContext context;

        AlbumPermissionFilter(AlbumUserPermissions permission, AlbumPermissionSecuredContext context) {
            this.permission = permission;
            this.context = context;
        }

        @Override
        public void filter(ContainerRequestContext requestContext) {

            if (permission != null) {
                final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)requestContext.getSecurityContext().getUserPrincipal());

                if(context.equals(AlbumPermissionSecuredContext.PATH_PARAM)) {
                    final MultivaluedMap<String, String> pathParam = requestContext.getUriInfo().getPathParameters();
                    if (pathParam.containsKey(ALBUM)) {
                        final String albumID = pathParam.get(ALBUM).get(0);
                        tryPermission(kheopsPrincipal, albumID, requestContext);
                    }
                } else if (context.equals(AlbumPermissionSecuredContext.QUERY_PARAM)) {

                    final MultivaluedMap<String, String> queryParam = requestContext.getUriInfo().getQueryParameters();
                    if (queryParam.containsKey(ALBUM)) {
                        final String albumID = queryParam.get(ALBUM).get(0);
                        tryPermission(kheopsPrincipal, albumID, requestContext);
                    }
                }
            }
        }

        private void tryPermission(KheopsPrincipal kheopsPrincipal, String albumID, ContainerRequestContext requestContext) {

            if (!kheopsPrincipal.hasAlbumPermission(permission, albumID)) {
                requestContext.abortWith(Response.status(FORBIDDEN).entity("Album ID : " + albumID + " Forbidden").build());
            }

        }
    }
}
