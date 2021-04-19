package online.kheops.auth_server.mapper;

import online.kheops.auth_server.album.AlbumNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Provider
public class AlbumNotFoundMapper implements ExceptionMapper<AlbumNotFoundException> {

    public Response toResponse(AlbumNotFoundException e) {
        return Response.status(NOT_FOUND).entity(e.getErrorResponse()).header(CONTENT_TYPE, APPLICATION_JSON).build();
    }
}
