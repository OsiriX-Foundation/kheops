package online.kheops.auth_server.entity;

import online.kheops.auth_server.report_provider.ClientId;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SuppressWarnings("unused")
@Entity
@Table(name = "report_providers")

public class ReportProvider {

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

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = true, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "user_fk", nullable=false, insertable = true, updatable = false)
    private User user;

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        creationTime = now;
    }

    public ReportProvider() {}

    public ReportProvider(String url, String name, Album album, User user) {
        this.clientId = new ClientId().getClientId();
        this.url = url;
        this.name = name;
        this.album = album;
        this.user = user;
        album.addReportProvider(this);
        user.addReportProvider(this);
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getClientId() {
        return clientId;
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

    public void setClientId(String clientId) { this.clientId = clientId; }

    public void setUrl(String url) { this.url = url; }

    public void setName(String name) { this.name = name; }
}
