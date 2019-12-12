package online.kheops.auth_server.capability;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class CapabilityBadRequestException extends Exception implements KheopsException {

    private ErrorResponse errorResponse;

    public CapabilityBadRequestException(ErrorResponse errorResponse) {
        super();
        this.errorResponse = errorResponse;
    }


    public ErrorResponse getErrorResponse() { return errorResponse; }
}
