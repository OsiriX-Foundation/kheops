package online.kheops.auth_server.mapper;

import online.kheops.auth_server.album.BadQueryParametersException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class BadQueryParameterMapper implements ExceptionMapper<BadQueryParametersException> {

    public Response toResponse(BadQueryParametersException e) {
        return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).header(CONTENT_TYPE, APPLICATION_JSON).build();
    }
}
