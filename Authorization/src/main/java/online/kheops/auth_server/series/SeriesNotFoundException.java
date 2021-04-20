package online.kheops.auth_server.series;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class SeriesNotFoundException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public SeriesNotFoundException (ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }
}
