package online.kheops.auth_server.album;

public class AlbumIdInvalidFormatException extends Exception{

    public AlbumIdInvalidFormatException(String message) {
        super(message);
    }

    public AlbumIdInvalidFormatException() {
        super("AlbumId Invalid Format");
    }

    public AlbumIdInvalidFormatException(String message, Throwable e) {
        super(message, e);
    }

    public AlbumIdInvalidFormatException(Throwable e) { super("AlbumId Invalid Format", e); }
}
