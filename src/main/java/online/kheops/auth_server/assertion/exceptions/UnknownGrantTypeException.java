package online.kheops.auth_server.assertion.exceptions;

public class UnknownGrantTypeException extends Exception {
    public UnknownGrantTypeException(String message) {
        super(message);
    }
}
