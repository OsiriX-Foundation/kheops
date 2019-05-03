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

    @Basic(optional = false)
    @Column(name = "sharing_date")
    private LocalDateTime sharingDate;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = false, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "series_fk", nullable=false, insertable = false, updatable = false)
    private Series series;

    @ManyToOne
    @JoinColumn (name = "sharing_source", nullable=true, insertable = false, updatable = false)
    private User sharingSource;

    public AlbumSeries() {}

    public AlbumSeries(Album album, Series series) {
        this.album = album;
        this.series = series;
        favorite = false;
    }

    public AlbumSeries(Album album, Series series, User user) {
        this.album = album;
        this.series = series;
        favorite = false;
        sharingSource = user;
        //user.set sharingsource TODO
    }

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        sharingDate = now;
    }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public boolean isFavorite() { return favorite; }
}
