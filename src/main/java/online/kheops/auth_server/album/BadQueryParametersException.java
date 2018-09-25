package online.kheops.auth_server.album;

public class BadQueryParametersException extends Exception{
    public BadQueryParametersException(String message) {
        super(message);
    }

    public BadQueryParametersException(String message, Throwable e) {
        super(message, e);
    }
}
