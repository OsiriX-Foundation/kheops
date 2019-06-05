package online.kheops.auth_server.accesstoken;

public final class BadAccessTokenException extends Exception {
    BadAccessTokenException(String message) {
        super(message);
    }
    BadAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
