package online.kheops.auth_server.webhook;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

import static online.kheops.auth_server.util.ErrorResponse.Message.WEBHOOK_NOT_FOUND;

public class WebhookNotFoundException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public WebhookNotFoundException() {
        super();
        errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message(WEBHOOK_NOT_FOUND)
                .detail("The webhook does not exist or you don't have access")
                .build();
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }
}
