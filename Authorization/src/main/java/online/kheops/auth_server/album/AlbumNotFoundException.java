package online.kheops.auth_server.album;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

import static online.kheops.auth_server.util.ErrorResponse.Message.ALBUM_NOT_FOUND;

public class AlbumNotFoundException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public AlbumNotFoundException() {
        super();
        errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message(ALBUM_NOT_FOUND)
                .detail("The album does not exist or you don't have access")
                .build();
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }
}
