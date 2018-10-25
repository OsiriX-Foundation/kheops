package online.kheops.auth_server.album;

public class UserNotMemberException extends Exception{

    public UserNotMemberException(String message) {
        super(message);
    }

    public UserNotMemberException() {
        super("User not a member of the album");
    }

    public UserNotMemberException(String message, Throwable e) {
        super(message, e);
    }

    public UserNotMemberException(Throwable e) { super("User not a member of the album", e); }
}
