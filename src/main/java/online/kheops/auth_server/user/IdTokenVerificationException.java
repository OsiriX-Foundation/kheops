package online.kheops.auth_server.user;

public final class IdTokenVerificationException extends Exception {
    IdTokenVerificationException(String message) {
        super(message);
    }
    IdTokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
