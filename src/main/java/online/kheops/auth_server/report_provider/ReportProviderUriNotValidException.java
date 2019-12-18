package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsException;

public class ReportProviderUriNotValidException extends Exception implements KheopsException {

    private final ErrorResponse errorResponse;

    public ReportProviderUriNotValidException(ErrorResponse errorResponse) {
        super();
        this.errorResponse = errorResponse;
    }

    public ReportProviderUriNotValidException(ErrorResponse errorResponse, Throwable e) {
        super( e);
        this.errorResponse = errorResponse;
    }

    @Override
    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
