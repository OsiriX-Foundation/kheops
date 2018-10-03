package online.kheops.auth_server.entity;

import online.kheops.auth_server.capability.Capabilities;
import online.kheops.auth_server.capability.CapabilityBadRequest;
import online.kheops.auth_server.capability.ScopeType;

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
        if(startTime == null) {
            startTime = now;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    private Capability() {}

    /*private Capability(User user, LocalDateTime expirationDate, LocalDateTime startDate, String title, boolean readPermission, boolean writePermission) {
        this.secret = Capabilities.newCapabilityToken();
        this.expiration = expirationDate;
        this.startTime = startDate;
        this.title  = title;
        this.user = user;
        this.scopeType = "user";
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        user.getCapabilities().add(this);
    }

    private Capability(User user, LocalDateTime expirationDate, LocalDateTime startDate, String title, Series series, boolean readPermission, boolean writePermission) {
        this(user, expirationDate, startDate, title, readPermission, writePermission);
        this.scopeType = "series";
        this.series = series;
        this.study = series.getStudy();
        series.addCapability(this);
        study.addCapability(this);
    }

    private Capability(User user, LocalDateTime expirationDate, LocalDateTime startDate, String title, Album album, boolean readPermission, boolean writePermission) {
        this(user, expirationDate, startDate, title, readPermission, writePermission);
        this.scopeType = "album";
        this.album = album;
        album.addCapability(this);
    }

    private Capability(User user, LocalDateTime expirationDate, LocalDateTime startDate, String title, Study study, boolean readPermission, boolean writePermission) {
        this(user, expirationDate, startDate, title, readPermission, writePermission);
        this.scopeType = "study";
        this.study = study;
        study.addCapability(this);
    }*/

    private Capability(CapabilityBuilder builder) throws CapabilityBadRequest {
        this.secret = Capabilities.newCapabilityToken();
        this.expiration = builder.expiration;
        this.startTime = builder.startTime;
        this.title  = builder.title;
        this.user = builder.user;
        builder.scopeType.setCapabilityEntityScope(this, builder.album, builder.study, builder.series);
        this.readPermission = builder.readPermission;
        this.writePermission = builder.writePermission;
        builder.user.getCapabilities().add(this);
    }

    public LocalDateTime getExpiration() { return expiration; }

    public void setExpiration(LocalDateTime expiration) { this.expiration = expiration; }

    public boolean isRevoked() { return revokedTime != null; }

    public void setRevoked(boolean revoked) {
        if (!revoked && this.revokedTime != null) {
            throw new IllegalStateException("Can't unrevoke a revoked capability");
        } else if (revoked && this.revokedTime == null) {
            this.revokedTime = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public LocalDateTime getRevokedTime() { return revokedTime; }

    public void setRevokedTime(LocalDateTime revokedTime) {
        if (this.revokedTime != null) {
            throw new IllegalStateException("Can't update the revokedTime on an already revoked capability");
        }
        this.revokedTime = revokedTime;
    }

    public String getTitle() { return title; }

    public void setTitle(String description) { this.title = description; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public long getPk() { return pk; }

    public LocalDateTime getCreatedTime() { return createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }

    public  LocalDateTime getStartTime() {return startTime; }

    public boolean isActive() {return startTime != null; }

    public String getSecret() { return secret; }

    public boolean isReadPermission() { return readPermission; }

    public boolean isWritePermission() { return writePermission; }

    public String getScopeType() { return scopeType; }

    public Album getAlbum() { return album; }

    public void setScopeType(String scopeType) { this.scopeType = scopeType; }

    public void setAlbum(Album album) { this.album = album; }

    public void setSeries(Series series) { this.series = series; }

    public void setStudy(Study study) { this.study = study; }

    public Series getSeries() { return series; }

    public Study getStudy() { return study; }

    public static class CapabilityBuilder {

        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;
        private LocalDateTime expiration;
        private LocalDateTime revokedTime;
        private LocalDateTime startTime;
        private String title;
        private boolean readPermission;
        private boolean writePermission;
        private User user;
        private ScopeType scopeType;
        private Album album;
        private Series series;
        private Study study;

        public CapabilityBuilder () {}

        public CapabilityBuilder createdTime (LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }
        public CapabilityBuilder updatedTime (LocalDateTime updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }
        public CapabilityBuilder expiration (LocalDateTime expiration) {
            this.expiration = expiration;
            return this;
        }
        public CapabilityBuilder revokedTime (LocalDateTime revokedTime) {
            this.revokedTime = revokedTime;
            return this;
        }
        public CapabilityBuilder startTime (LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }
        public CapabilityBuilder title (String title) {
            this.title = title;
            return this;
        }
        public CapabilityBuilder readPermission (boolean readPermission) {
            this.readPermission = readPermission;
            return this;
        }
        public CapabilityBuilder writePermission (boolean writePermission) {
            this.writePermission = writePermission;
            return this;
        }
        public CapabilityBuilder user (User user) {
            this.user = user;
            return this;
        }
        public CapabilityBuilder scopeType (ScopeType scopeType) {
            this.scopeType = scopeType;
            return this;
        }
        public CapabilityBuilder album (Album album) {
            this.album = album;
            return this;
        }
        public CapabilityBuilder series (Series series) {
            this.series = series;
            return this;
        }
        public CapabilityBuilder study (Study study) {
            this.study = study;
            return this;
        }

        public Capability build() throws CapabilityBadRequest{
            if (user == null) {
                throw new IllegalStateException("Missing user");
            }
            if (expiration == null) {
                throw new IllegalStateException("Missing expiration");
            }
            if (startTime == null) {
                throw new IllegalStateException("Missing startTime");
            }
            if (title == null) {
                throw new IllegalStateException("Missing title");
            }
            return new Capability(this);
        }
    }
}
