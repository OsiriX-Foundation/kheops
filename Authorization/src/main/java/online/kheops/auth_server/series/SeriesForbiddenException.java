package online.kheops.auth_server.series;

public class SeriesForbiddenException extends Exception{
    public SeriesForbiddenException(String message) {
        super(message);
    }

    public SeriesForbiddenException(String message, Throwable e) {
        super(message, e);
    }
}
