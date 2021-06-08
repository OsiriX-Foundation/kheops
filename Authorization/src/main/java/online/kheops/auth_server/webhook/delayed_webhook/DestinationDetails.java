package online.kheops.auth_server.webhook.delayed_webhook;

import online.kheops.auth_server.entity.Album;

import java.util.Objects;

public class DestinationDetails {

    private long destinationPk;

    public DestinationDetails(Album destination) { this.destinationPk = destination.getPk(); }

    public long getAlbumPk() { return destinationPk; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DestinationDetails destinationDetails = (DestinationDetails) o;
        return destinationDetails.destinationPk == this.destinationPk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(destinationPk);
    }
}
