package online.kheops.auth_server.entity;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "album_series")
public class AlbumSeries {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "favorite")
    private boolean favorite   = true;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = false, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "series_fk", nullable=false, insertable = false, updatable = false)
    private Series series;

    public AlbumSeries() {}

    public AlbumSeries(Album album, Series series) {
        this.album = album;
        this.series = series;
        favorite = false;
    }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public boolean isFavorite() { return favorite; }
}
