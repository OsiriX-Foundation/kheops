package online.kheops.auth_server.album;

import online.kheops.auth_server.util.ErrorResponse;

public class BadQueryParametersException extends Exception{

    private final ErrorResponse errorResponse;

    public BadQueryParametersException(ErrorResponse errorResponse ) {
        super(errorResponse.toString());
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {return errorResponse; }
}
