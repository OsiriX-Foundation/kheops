package online.kheops.auth_server.mapper;

import online.kheops.auth_server.album.AlbumForbiddenException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Provider
public class AlbumForbiddenMapper implements ExceptionMapper<AlbumForbiddenException> {

    public Response toResponse(AlbumForbiddenException e) {
        return Response.status(FORBIDDEN).entity(e.getErrorResponse()).header(CONTENT_TYPE, APPLICATION_JSON).build();
    }
}
