package online.kheops.auth_server.mapper;

import online.kheops.auth_server.util.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


@Provider
public class OtherExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(OtherExceptionMapper.class.getName());

    public Response toResponse(Exception e) {

        final String id = newId();

        LOG.log(Level.SEVERE, String.format("id:%s", id), e);

        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message("Internal server error")
                .detail("log id : " + id)
                .build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }

    private static final String DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int ID_LENGTH = 20;
    private static final Random rdm = new SecureRandom();

    private  String newId() {
        final StringBuilder idBuilder = new StringBuilder();

        idBuilder.setLength(0);
        while (idBuilder.length() < ID_LENGTH) {
            int index = rdm.nextInt(DICT.length());
            idBuilder.append(DICT.charAt(index));
        }
        return idBuilder.toString();
    }
}
