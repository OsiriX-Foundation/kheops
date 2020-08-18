package online.kheops.auth_server.report_provider;

public class ReportProviderNotFoundException extends Exception {
    public ReportProviderNotFoundException() {
    }

    public ReportProviderNotFoundException(String message) {
        super(message);
    }

    public ReportProviderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportProviderNotFoundException(Throwable cause) {
        super(cause);
    }

    protected ReportProviderNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
