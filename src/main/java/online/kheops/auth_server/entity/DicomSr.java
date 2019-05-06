package online.kheops.auth_server.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Entity
@Table(name = "dicom_sr")

public class DicomSr {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Basic(optional = false)
    @Column(name = "client_id")
    private String clientId;

    @Basic(optional = true)
    @Column(name = "client_secret")
    private String clientSecret;

    @Basic(optional = false)
    @Column(name = "private")
    private Boolean isPrivate;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = false, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "user_fk", nullable=false, insertable = false, updatable = false)
    private User user;

    public DicomSr(String clientId, String clientSecret, String url, String name, Album album, User user) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.url = url;
        this.name = name;
        this.album = album;
        this.user = user;
        album.addDicomSr(this);
        user.addDicomSr(this);
    }

    public DicomSr(String clientId, String url, String name, Album album, User user) {
        this.clientId = clientId;
        this.url = url;
        this.name = name;
        this.album = album;
        this.user = user;
        album.addDicomSr(this);
        user.addDicomSr(this);
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Boolean isPrivate() {
        return isPrivate;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Album getAlbum() {
        return album;
    }

    public User getUser() {
        return user;
    }
}
