package online.kheops.proxy.stow.authorization;

public class AuthorizationManagerException extends Exception {
    public enum Reason {
        MISSING_ATTRIBUTE,
        SERIES_ACCESS_FORBIDDEN,
        UNKNOWN_CONTENT_LOCATION
    }

    private final Reason reason;

    AuthorizationManagerException(String message, Reason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }


    AuthorizationManagerException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Reason:" + getReason() + "\n" + super.toString();
    }

    private Reason getReason() {
        return reason;
    }
}
