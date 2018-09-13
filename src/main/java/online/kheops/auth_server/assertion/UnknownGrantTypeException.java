package online.kheops.auth_server.assertion;

public final class UnknownGrantTypeException extends Exception {
    UnknownGrantTypeException(String message) {
        super(message);
    }
}
