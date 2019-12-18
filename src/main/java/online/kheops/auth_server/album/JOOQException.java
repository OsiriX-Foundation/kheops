package online.kheops.auth_server.album;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class JOOQException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public JOOQException(String message)
    {
        super(message);
        errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message("Error with JOOQ")
                .detail("An error occured when requesting the DB with JOOQ")
                .build();
    }
    public JOOQException(String message, Throwable cause) {
        super(message, cause);
        errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message("Error with JOOQ")
                .detail("An error occured when requesting the DB with JOOQ")
                .build();
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }
}
