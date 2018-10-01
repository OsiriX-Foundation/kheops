package online.kheops.auth_server.entity;

import online.kheops.auth_server.capability.Capabilities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SuppressWarnings("unused")
@Entity
@Table(name = "capabilities")

public class Capability {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Basic(optional = false)
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "expiration_time")
    private LocalDateTime expiration;

    @Column(name = "revoked_time")
    private LocalDateTime revokedTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Basic(optional = false)
    @Column(name = "title")
    private String title;

    @Basic(optional = false)
    @Column(name = "secret", updatable = false)
    private String secret;

    @Basic(optional = false)
    @Column(name = "read_permission ", updatable = false)
    private boolean readPermission ;

    @Basic(optional = false)
    @Column(name = "write_permission ", updatable = false)
    private boolean writePermission ;

    @ManyToOne
    @JoinColumn(name = "user_fk", insertable=false, updatable=false)
    private User user;

    @Basic(optional = false)
    @Column(name = "scope_type ", updatable = false)
    private String scopeType ;

    @ManyToOne
    @JoinColumn(name = "album_fk ", insertable=false, updatable=false)
    private Album album;

    @ManyToOne
    @JoinColumn(name = "series_fk ", insertable=false, updatable=false)
    private Series series;

    @ManyToOne
    @JoinColumn(name = "study_fk ", insertable=false, updatable=false)
    private Study study;

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        createdTime = now;
        updatedTime = now;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Capability() {
        this.secret = Capabilities.newCapabilityToken();
    }

    public Capability(User user, LocalDateTime expirationDate, String title, Series series, boolean readPermission, boolean writePermission) {
        this.secret = Capabilities.newCapabilityToken();
        this.expiration = expirationDate;
        this.title  = title;
        this.user = user;
        this.scopeType = "series";
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        user.getCapabilities().add(this);
        series.addCapability(this);
    }

    public Capability(User user, LocalDateTime expirationDate, String title, Album album, boolean readPermission, boolean writePermission) {
        this.secret = Capabilities.newCapabilityToken();
        this.expiration = expirationDate;
        this.title  = title;
        this.user = user;
        this.scopeType = "album";
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        user.getCapabilities().add(this);
        album.addCapability(this);
    }

    public Capability(User user, LocalDateTime expirationDate, String title, Study study, boolean readPermission, boolean writePermission) {
        this.secret = Capabilities.newCapabilityToken();
        this.expiration = expirationDate;
        this.title  = title;
        this.user = user;
        this.scopeType = "study";
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        user.getCapabilities().add(this);
        study.addCapability(this);
    }

    public Capability(User user, LocalDateTime expirationDate, String title, boolean readPermission, boolean writePermission) {
        this.secret = Capabilities.newCapabilityToken();
        this.expiration = expirationDate;
        this.title  = title;
        this.user = user;
        this.scopeType = "user";
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        user.getCapabilities().add(this);
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public boolean isRevoked() {
        return revokedTime != null;
    }

    public void setRevoked(boolean revoked) {
        if (!revoked && this.revokedTime != null) {
            throw new IllegalStateException("Can't unrevoke a revoked capability");
        } else if (revoked && this.revokedTime == null) {
            this.revokedTime = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public LocalDateTime getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(LocalDateTime revokedTime) {
        if (this.revokedTime != null) {
            throw new IllegalStateException("Can't update the revokedTime on an already revoked capability");
        }
        this.revokedTime = revokedTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String description) {
        this.title = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getPk() {
        return pk;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public  LocalDateTime getStartTime() {return startTime; }

    public boolean isActive() {return startTime != null; }

    public String getSecret() {
        return secret;
    }

    public boolean isReadPermission() { return readPermission; }

    public boolean isWritePermission() { return writePermission; }

    public String getScopeType() { return scopeType; }

    public Album getAlbum() { return album; }

    public Series getSeries() { return series; }

    public Study getStudy() { return study; }
}
