package online.kheops.auth_server.filter;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.AlbumAccessSecured;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.ALBUM_ACCESS_PRIORITY;

@AlbumAccessSecured
@Provider
@Priority(ALBUM_ACCESS_PRIORITY)
public class AlbumAccessSecuredFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(AlbumAccessSecuredFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)requestContext.getSecurityContext().getUserPrincipal());

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

    private void tryAccess(KheopsPrincipalInterface kheopsPrincipal, String albumID, ContainerRequestContext requestContext) {
        try {
            if (!kheopsPrincipal.hasAlbumAccess(albumID)) {
                requestContext.abortWith(Response.status(NOT_FOUND).entity("Album ID : " + albumID + " Not Found").build());
            }
        } catch (AlbumNotFoundException e) {
            requestContext.abortWith(Response.status(NOT_FOUND).entity("Album ID : " + albumID + " Not Found").build());
        }
    }
}