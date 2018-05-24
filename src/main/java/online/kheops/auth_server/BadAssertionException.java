package online.kheops.auth_server;

public class BadAssertionException extends Exception {
    public BadAssertionException(String message) {
        super(message);
    }
    public BadAssertionException(String message, Throwable cause) {
        super(message, cause);
    }
}
