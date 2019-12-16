package online.kheops.auth_server.album;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

import static online.kheops.auth_server.util.ErrorResponse.Message.ALBUM_NOT_FOUND;

public class UserNotMemberException extends Exception implements KheopsException {

    private ErrorResponse errorResponse;

    public UserNotMemberException() {
        super();
        errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message(ALBUM_NOT_FOUND)
                .detail("The album does not exist or you don't have access")
                .build();
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }

}
