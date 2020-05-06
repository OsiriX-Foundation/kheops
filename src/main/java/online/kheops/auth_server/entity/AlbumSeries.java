package online.kheops.auth_server.entity;


import javax.persistence.*;

@SuppressWarnings({"WeakerAccess", "unused"})
@NamedQueries({
        @NamedQuery(name = "AlbumSeries.findByAlbumIdAndSeriesUID",
                query = "SELECT aSeries FROM Album a JOIN a.albumSeries aSeries JOIN aSeries.series s WHERE :seriesUID = s.seriesInstanceUID AND :albumID = a.id")
})

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
    @JoinColumn (name = "album_fk", nullable=false, insertable = true, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "series_fk", nullable=false, insertable = true, updatable = false)
    private Series series;

    public AlbumSeries() {}

    public AlbumSeries(Album album, Series series) {
        this.album = album;
        this.series = series;
        favorite = false;
        this.album.addSeries(this);
        this.series.addAlbumSeries(this);
    }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public boolean isFavorite() { return favorite; }
}
