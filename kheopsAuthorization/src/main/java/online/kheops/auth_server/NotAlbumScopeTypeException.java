package online.kheops.auth_server;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class NotAlbumScopeTypeException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public NotAlbumScopeTypeException(ErrorResponse errorResponse) {
        super();
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
