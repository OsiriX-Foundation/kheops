package online.kheops.auth_server;

public class DownloadKeyException extends RuntimeException {
    public DownloadKeyException(String message) {
        super(message);
    }
    public DownloadKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
