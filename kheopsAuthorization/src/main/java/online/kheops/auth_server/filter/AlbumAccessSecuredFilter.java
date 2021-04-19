package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.AlbumAccessSecured;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.util.ErrorResponse;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.ALBUM_ACCESS_PRIORITY;
import static online.kheops.auth_server.util.ErrorResponse.Message.ALBUM_NOT_FOUND;

@AlbumAccessSecured
@Provider
@Priority(ALBUM_ACCESS_PRIORITY)
public class AlbumAccessSecuredFilter implements ContainerRequestFilter {

    private static final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
            .message(ALBUM_NOT_FOUND)
            .detail("The album does not exist or you don't have access")
            .build();

    @Override
    public void filter(ContainerRequestContext requestContext) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)requestContext.getSecurityContext().getUserPrincipal());

        final MultivaluedMap<String, String> pathParam = requestContext.getUriInfo().getPathParameters();
        if(pathParam.containsKey(ALBUM)) {
            final String albumID = pathParam.get(ALBUM).get(0);
            tryAccess(kheopsPrincipal, albumID, requestContext);
        }

        final MultivaluedMap<String, String> queryParam = requestContext.getUriInfo().getQueryParameters();
        if(queryParam.containsKey(ALBUM)) {
            final String albumID = queryParam.get(ALBUM).get(0);
            tryAccess(kheopsPrincipal, albumID, requestContext);
        }
    }

    private void tryAccess(KheopsPrincipal kheopsPrincipal, String albumID, ContainerRequestContext requestContext) {

        if (!kheopsPrincipal.hasAlbumAccess(albumID)) {
            requestContext.abortWith(Response.status(NOT_FOUND).header(CONTENT_TYPE, APPLICATION_JSON).entity(errorResponse).build());
        }
    }
}