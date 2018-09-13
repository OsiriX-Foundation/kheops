package online.kheops.auth_server.assertion;

public final class BadAssertionException extends Exception {
    BadAssertionException(String message) {
        super(message);
    }
    BadAssertionException(String message, Throwable cause) {
        super(message, cause);
    }
}
