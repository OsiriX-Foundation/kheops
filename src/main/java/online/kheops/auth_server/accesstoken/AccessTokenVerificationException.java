package online.kheops.auth_server.accesstoken;

public final class AccessTokenVerificationException extends Exception {
    AccessTokenVerificationException(String message) {
        super(message);
    }
    AccessTokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
