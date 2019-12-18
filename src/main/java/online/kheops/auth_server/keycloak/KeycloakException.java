package online.kheops.auth_server.keycloak;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class KeycloakException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public KeycloakException(ErrorResponse errorResponse) {

        super();
        this.errorResponse = errorResponse;
    }

    public KeycloakException(ErrorResponse errorResponse, Throwable e) {

        super(e);
        this.errorResponse = errorResponse;
    }

    @Override
    public ErrorResponse getErrorResponse() { return errorResponse; }
}
