package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.user.AlbumUserPermissions;
import online.kheops.auth_server.util.ErrorResponse;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.ALBUM_PERMISSION_ACCESS_PRIORITY;
import static online.kheops.auth_server.util.ErrorResponse.Message.AUTHORIZATION_ERROR;

@Provider
public class AlbumPermissionFilterFactory implements DynamicFeature {

    private static final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
            .message(AUTHORIZATION_ERROR)
            .detail("You don't have the permission to do this on this album")
            .build();

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext featureContext) {

        AlbumPermissionSecured aps = resourceInfo.getResourceMethod().getAnnotation(AlbumPermissionSecured.class);
        if (aps != null) {
            if (aps.context().equals(AlbumPermissionSecuredContext.PATH_PARAM)) {
                featureContext.register(new AlbumPermissionFilterPP(aps.permission()));
            } else if (aps.context().equals(AlbumPermissionSecuredContext.QUERY_PARAM)) {
                featureContext.register(new AlbumPermissionFilterQP(aps.permission()));
            }
        }

        AlbumPermissionSecured.List aps2 = resourceInfo.getResourceMethod().getAnnotation(AlbumPermissionSecured.List.class);
        if (aps2 != null) {
            for (int i =0; i<aps2.value().length; i++) {
                if (aps2.value()[i].context().equals(AlbumPermissionSecuredContext.PATH_PARAM)) {
                    featureContext.register(new AlbumPermissionFilterPP(aps2.value()[i].permission()));
                } else if (aps2.value()[i].context().equals(AlbumPermissionSecuredContext.QUERY_PARAM)) {
                    featureContext.register(new AlbumPermissionFilterQP(aps2.value()[i].permission()));
                }
            }
        }
    }

    @Priority(ALBUM_PERMISSION_ACCESS_PRIORITY)
    private static class AlbumPermissionFilterQP implements ContainerRequestFilter {
        private final AlbumUserPermissions permission;

        AlbumPermissionFilterQP(AlbumUserPermissions permission) {
            this.permission = permission;
        }

        @Override
        public void filter(ContainerRequestContext requestContext) {

        if (permission != null) {
            final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)requestContext.getSecurityContext().getUserPrincipal());

            final MultivaluedMap<String, String> queryParam = requestContext.getUriInfo().getQueryParameters();
            if (queryParam.containsKey(ALBUM)) {
                final String albumID = queryParam.get(ALBUM).get(0);
                if (!kheopsPrincipal.hasAlbumPermission(permission, albumID)) {
                    requestContext.abortWith(Response.status(FORBIDDEN).entity(errorResponse).header(CONTENT_TYPE, APPLICATION_JSON).build());
                }
            }
            }
        }
    }


    @Priority(ALBUM_PERMISSION_ACCESS_PRIORITY)
    private static class AlbumPermissionFilterPP implements ContainerRequestFilter {
        private final AlbumUserPermissions permission;

        AlbumPermissionFilterPP(AlbumUserPermissions permission) {
            this.permission = permission;
        }

        @Override
        public void filter(ContainerRequestContext requestContext) {

            if (permission != null) {
                final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)requestContext.getSecurityContext().getUserPrincipal());

                final MultivaluedMap<String, String> pathParam = requestContext.getUriInfo().getPathParameters();
                if (pathParam.containsKey(ALBUM)) {
                    final String albumID = pathParam.get(ALBUM).get(0);
                    if (!kheopsPrincipal.hasAlbumPermission(permission, albumID)) {
                        requestContext.abortWith(Response.status(FORBIDDEN).entity(errorResponse).header(CONTENT_TYPE, APPLICATION_JSON).build());
                    }
                }
            }
        }
    }

}
