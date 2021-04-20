package online.kheops.auth_server.album;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

import static online.kheops.auth_server.util.ErrorResponse.Message.ALBUM_NOT_FOUND;

public class UserNotMemberException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public UserNotMemberException() {
        super();
        errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message(ALBUM_NOT_FOUND)
                .detail("The album does not exist or you don't have access")
                .build();
    }

    public UserNotMemberException(ErrorResponse errorResponse) {
        super();
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }

}
