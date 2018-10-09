package online.kheops.proxy.tokens;

public class AccessTokenException extends Exception {
    public AccessTokenException(String message) {
        super(message);
    }

    public AccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
