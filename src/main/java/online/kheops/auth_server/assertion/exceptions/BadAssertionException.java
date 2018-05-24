package online.kheops.auth_server.assertion.exceptions;

public class BadAssertionException extends Exception {
    public BadAssertionException(String message) {
        super(message);
    }
    public BadAssertionException(String message, Throwable cause) {
        super(message, cause);
    }
}
