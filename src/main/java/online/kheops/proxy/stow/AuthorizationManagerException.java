package online.kheops.proxy.stow;

public class AuthorizationManagerException extends Exception {
    public enum Reason {
        MISSING_ATTRIBUTE,
        SERIES_ACCESS_FORBIDDEN,
        UNKNOWN_CONTENT_LOCATION
    }

    private final Reason reason;

    public AuthorizationManagerException(String message, Reason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }


    public AuthorizationManagerException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
