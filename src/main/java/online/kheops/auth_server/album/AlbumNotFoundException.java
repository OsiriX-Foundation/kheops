package online.kheops.auth_server.album;

public class AlbumNotFoundException extends Exception{

    public AlbumNotFoundException(String message) {
        super(message);
    }

    public AlbumNotFoundException() {
        super("Unknown album");
    }

    public AlbumNotFoundException(String message, Throwable e) {
        super(message, e);
    }

    public AlbumNotFoundException(Throwable e) { super("Unknown album", e); }
}
