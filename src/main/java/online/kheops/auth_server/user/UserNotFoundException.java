package online.kheops.auth_server.user;

public class UserNotFoundException extends Exception{

    public UserNotFoundException(String message) { super(message); }

    public UserNotFoundException() { super("Unknown user"); }

    public UserNotFoundException(String message, Throwable e) {
        super(message, e);
    }

    public UserNotFoundException(Throwable e) {
        super("Unknown user", e);
    }
}
