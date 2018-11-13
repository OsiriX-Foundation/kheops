package online.kheops.auth_server.filter;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.annotation.CacheControlHeader;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.user.UsersPermission;

import javax.ws.rs.container.*;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.HttpHeaders.CACHE_CONTROL;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Provider
public class AlbumPermissionFilterFactory implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        AlbumPermissionSecured aps = resourceInfo.getResourceMethod().getAnnotation(AlbumPermissionSecured.class);
        if (aps != null) {
            featureContext.register(new AlbumPermissionFilter(aps.value()));
        }
    }

    private static class AlbumPermissionFilter implements ContainerRequestFilter {
        private final UsersPermission.UsersPermissionEnum permission;

        AlbumPermissionFilter(UsersPermission.UsersPermissionEnum permission) {
            this.permission = permission;
        }

        @Override
        public void filter(ContainerRequestContext requestContext) {

            if (permission != null) {
                final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)requestContext.getSecurityContext().getUserPrincipal());

                final MultivaluedMap<String, String> pathParam = requestContext.getUriInfo().getPathParameters();
                if(pathParam.containsKey("album")) {
                    final Long albumID = Long.valueOf(pathParam.get("album").get(0));
                    tryPermission(kheopsPrincipal, albumID, requestContext);
                }

                final MultivaluedMap<String, String> queryParam = requestContext.getUriInfo().getQueryParameters();
                //TODO album or albums ??
                if(queryParam.containsKey("album")) {
                    final Long albumID = Long.valueOf(queryParam.get("album").get(0));
                    tryPermission(kheopsPrincipal, albumID, requestContext);
                }
            }
        }

        private void tryPermission(KheopsPrincipalInterface kheopsPrincipal, Long albumID, ContainerRequestContext requestContext) {
            try {
                if (!kheopsPrincipal.hasAlbumPermission(permission, albumID)) {
                    requestContext.abortWith(Response.status(FORBIDDEN).entity("Album ID : " + albumID + " Forbidden").build());
                }
            } catch (AlbumNotFoundException e) {
                requestContext.abortWith(Response.status(NOT_FOUND).entity("Album ID : " + albumID + " Not Found").build());
            }
        }
    }
}
