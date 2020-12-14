package online.kheops.auth_server.webhook.delayed_webhook;

import online.kheops.auth_server.entity.Album;

import java.util.Objects;

public class DestinationDetails {

    private Album destination;

    public DestinationDetails(Album destination) { this.destination = destination; }

    public Album getAlbum() { return destination; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DestinationDetails destinationDetails = (DestinationDetails) o;
        return destinationDetails.destination.equals(this.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination);
    }
}
