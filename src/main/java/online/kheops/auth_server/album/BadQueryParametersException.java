package online.kheops.auth_server.album;

import online.kheops.auth_server.util.ErrorResponse;

public class BadQueryParametersException extends Exception{

    private ErrorResponse errorResponse;

    public BadQueryParametersException(String message) {
        super(message);
    }

    public BadQueryParametersException(ErrorResponse errorResponse ) {
        super(errorResponse.toString());
        this.errorResponse = errorResponse;
    }

    public BadQueryParametersException(String message, Throwable e) {
        super(message, e);
    }

    public ErrorResponse getErrorResponse() {return errorResponse; }
}
