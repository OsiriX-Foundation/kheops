package online.kheops.auth_server.capability;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class NewCapabilityForbidden extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public NewCapabilityForbidden() {
        super();
        this.errorResponse = new ErrorResponse.ErrorResponseBuilder()
                .message("Not admin")
                .detail("Only an admin can generate a capability token for an album")
                .build();
    }

    @Override
    public ErrorResponse getErrorResponse() { return errorResponse; }

}
