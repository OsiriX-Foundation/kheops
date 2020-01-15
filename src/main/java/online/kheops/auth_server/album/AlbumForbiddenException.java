package online.kheops.auth_server.album;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class AlbumForbiddenException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public AlbumForbiddenException(ErrorResponse errorResponse) {
        super(errorResponse.toString());
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }

}
