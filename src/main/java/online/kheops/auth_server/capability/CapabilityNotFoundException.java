package online.kheops.auth_server.capability;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class CapabilityNotFoundException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public CapabilityNotFoundException(ErrorResponse errorResponse) {
        super();
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }
}
