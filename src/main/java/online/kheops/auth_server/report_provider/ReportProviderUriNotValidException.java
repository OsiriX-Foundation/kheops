package online.kheops.auth_server.report_provider;

public class ReportProviderUriNotValidException extends Exception{

    public ReportProviderUriNotValidException(String message) {
        super(message);
    }

    public ReportProviderUriNotValidException(String message, Throwable e) {
        super(message, e);
    }

}
