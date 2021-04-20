package online.kheops.auth_server.album;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class BadQueryParametersException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public BadQueryParametersException(ErrorResponse errorResponse ) {
        super(errorResponse.toString());
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {return errorResponse; }
}
