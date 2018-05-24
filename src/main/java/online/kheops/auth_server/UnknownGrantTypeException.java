package online.kheops.auth_server;

public class UnknownGrantTypeException extends Exception {
    public UnknownGrantTypeException(String message) {
        super(message);
    }
}
