package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class ReportProviderNotFoundException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public ReportProviderNotFoundException(ErrorResponse errorResponse) {

        super();
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
