package online.kheops.auth_server.study;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class StudyNotFoundException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public StudyNotFoundException(ErrorResponse errorResponse) {

        super();
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() { return errorResponse; }
}
