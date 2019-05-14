package online.kheops.auth_server.entity;


import javax.persistence.*;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "album_user")

public class AlbumUser {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "new_series_notifications")
    private boolean newSeriesNotifications = true;

    @Basic(optional = false)
    @Column(name = "new_comment_notifications")
    private boolean newCommentNotifications = true;

    @Basic(optional = false)
    @Column(name = "favorite")
    private boolean favorite = false;

    @Basic(optional = false)
    @Column(name = "admin")
    private boolean admin = false;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = false, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "user_fk", nullable=false, insertable = false, updatable = false)
    private User user;

    public AlbumUser () {}

    public AlbumUser (Album album, User user, Boolean isAdmin) {
        this.album = album;
        this.user = user;
        this.admin = isAdmin;

        album.addAlbumUser(this);
        user.addAlbumUser(this);
    }

    public long getPk() { return pk; }

    public void setPk(long pk) { this.pk = pk; }

    public boolean isNewSeriesNotifications() { return newSeriesNotifications; }

    public void setNewSeriesNotifications( boolean newSeriesNotifications ) { this.newSeriesNotifications = newSeriesNotifications; }

    public boolean isNewCommentNotifications() { return newCommentNotifications; }

    public void setNewCommentNotifications( boolean newCommentNotifications ) { this.newCommentNotifications = newCommentNotifications; }

    public boolean isFavorite() { return favorite; }

    public void setFavorite( Boolean favorite ) { this.favorite = favorite; }

    public boolean isAdmin() { return admin; }

    public void setAdmin( Boolean admin ) { this.admin = admin; }

    public Album getAlbum() { return album; }

    public User getUser() { return user; }

}
