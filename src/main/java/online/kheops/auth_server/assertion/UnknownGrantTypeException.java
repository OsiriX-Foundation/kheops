package online.kheops.auth_server.assertion;

public class UnknownGrantTypeException extends Exception {
    UnknownGrantTypeException(String message) {
        super(message);
    }
}
