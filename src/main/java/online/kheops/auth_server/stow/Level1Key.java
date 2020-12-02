package online.kheops.auth_server.stow;

import online.kheops.auth_server.entity.Album;

import java.util.Objects;

public class Level1Key {

    private Album destination;

    public Level1Key(Album destination) {
        this.destination = destination;
    }

    public Album getAlbum() { return destination; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level1Key level1Key = (Level1Key) o;
        return level1Key.destination.equals(destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination);
    }
}
