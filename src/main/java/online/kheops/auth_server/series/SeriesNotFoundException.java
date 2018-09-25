package online.kheops.auth_server.series;

public class SeriesNotFoundException extends Exception{
    public SeriesNotFoundException(String message) {
        super(message);
    }

    public SeriesNotFoundException(String message, Throwable e) {
        super(message, e);
    }
}
