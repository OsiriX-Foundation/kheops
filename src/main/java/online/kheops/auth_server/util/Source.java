package online.kheops.auth_server.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Source {
    private final String albumId;
    private static final Source INBOX = new Source(null);

    private Source(String albumId) {
        this.albumId = albumId;
    }

    public static Source inbox() {
        return INBOX;
    }

    public static Source album(String albumId) {
        if (albumId == null || albumId.isEmpty()) {
            throw new IllegalArgumentException("empty albumID");
        }
        return new Source(albumId);
    }

    public static Source instance(boolean isInbox, String albumId) {
        if (!isInbox && (albumId == null || albumId.isEmpty())) {
            throw new IllegalArgumentException("not an inbox, but also no album id");
        } else if (isInbox && !(albumId == null || albumId.isEmpty())) {
            throw new IllegalArgumentException("is inbox, but also has an album id");
        }

        if (isInbox) {
            return inbox();
        } else {
            return album(albumId);
        }
    }

    public boolean isInbox() {
        return equals(INBOX);
    }

    public String getAlbumId() {
        if (isInbox()) {
            throw new NoSuchElementException("Only album source types have an album ID");
        }

        return albumId;
    }

    public String getAlbumIdOrElse(String other) {
        if (isInbox()) {
            return other;
        } else {
            return getAlbumId();
        }
    }

    @SuppressWarnings("unused")
    public String getAlbumIdOrElseGet(Supplier<? extends String> supplier) {
        if (isInbox()) {
            return supplier.get();
        } else {
            return getAlbumId();
        }
    }

    @SuppressWarnings("unused")
    public <X extends Throwable> String getAlbumIdOrElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isInbox()) {
            throw exceptionSupplier.get();
        } else {
            return getAlbumId();
        }
    }

    @SuppressWarnings("unused")
    public void ifInbox(Runnable action) {
        if (isInbox()) {
            action.run();
        }
    }

    @SuppressWarnings("unused")
    public void ifAlbum(Consumer<? super String> action) {
        if (!isInbox()) {
            action.accept(albumId);
        }
    }

    @SuppressWarnings("unused")
    public void ifInboxOrAlbum(Runnable inboxAction, Consumer<? super String> albumAction) {
        if (isInbox()) {
            inboxAction.run();
        } else {
            albumAction.accept(albumId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Source source = (Source) o;
        return Objects.equals(albumId, source.albumId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(albumId);
    }

    @Override
    public String toString() {
        if (isInbox()) {
            return "Source{INBOX}";
        } else {
            return "Source{" + "albumId='" + albumId + '\'' + '}';
        }
    }
}
