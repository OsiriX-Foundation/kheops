package online.kheops.auth_server.album;

public class JOOQException extends Exception{
    public JOOQException(String message) {
        super(message);
    }
    public JOOQException(String message, Throwable cause) {
        super(message, cause);
    }
}
