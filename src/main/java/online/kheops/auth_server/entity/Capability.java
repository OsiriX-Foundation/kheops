package online.kheops.auth_server.entity;

import online.kheops.auth_server.capability.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static online.kheops.auth_server.capability.CapabilityToken.hashCapability;
import static online.kheops.auth_server.util.Consts.CAPABILITY_LEEWAY_SECOND;

@SuppressWarnings("unused")
@Entity
@Table(name = "capabilities")

public class Capability {

    @Transient
    private String secretBeforeHash;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "id")
    private String id;

    @Basic(optional = false)
    @Column(name = "issued_at_time", updatable = false)
    private LocalDateTime issuedAtTime;

    @Basic(optional = false)
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Column(name = "revoked_time")
    private LocalDateTime revokedTime;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "not_before_time")
    private LocalDateTime notBeforeTime;

    @Basic(optional = false)
    @Column(name = "title")
    private String title;

    @Basic(optional = false)
    @Column(name = "secret", updatable = false)
    private String secret;

    @Basic(optional = false)
    @Column(name = "read_permission ", updatable = false)
    private boolean readPermission ;

    @Basic
    @Column(name = "appropriate_permission ", updatable = false)
    private boolean appropriatePermission ;

    @Basic
    @Column(name = "download_permission ", updatable = false)
    private boolean downloadPermission ;

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

    @OneToMany
    @JoinColumn (name = "capability_fk")
    private Set<Mutation> mutations = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        issuedAtTime = now;
        updatedTime = now;
        id = new CapabilityId().getId();
        if(notBeforeTime == null) {
            notBeforeTime = now;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    private Capability() {}

    private Capability(CapabilityBuilder builder) throws CapabilityBadRequestException {
        secretBeforeHash = new CapabilityToken().getToken();
        this.secret = hashCapability(secretBeforeHash);
        this.expirationTime = builder.expirationTime;
        this.notBeforeTime = builder.notBeforeTime;
        this.title  = builder.title;
        this.user = builder.user;
        builder.scopeType.setCapabilityEntityScope(this, builder.album, builder.study, builder.series);
        this.readPermission = builder.readPermission;
        this.writePermission = builder.writePermission;
        this.appropriatePermission = builder.appropriatePermission;
        this.downloadPermission = builder.downloadPermission;
        builder.user.getCapabilities().add(this);
    }

    public LocalDateTime getExpirationTime() { return expirationTime; }

    public void setExpirationTime(LocalDateTime expirationTime) { this.expirationTime = expirationTime; }

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

    public String getId() { return id; }

    public void setTitle(String description) { this.title = description; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public long getPk() { return pk; }

    public LocalDateTime getIssuedAtTime() { return issuedAtTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }

    public  LocalDateTime getNotBeforeTime() {return notBeforeTime; }

    public LocalDateTime getLastUsed() { return lastUsed; }

    public void setLastUsed() { this.lastUsed = LocalDateTime.now(ZoneOffset.UTC); }

    public boolean isActive() {return notBeforeTime != null; }

    public void isValid() throws CapabilityNotValidException {
        if (isRevoked()) {
            throw new CapabilityNotValidException("Capability token is revoked");
        }
        if (ZonedDateTime.of(getNotBeforeTime().minusSeconds(CAPABILITY_LEEWAY_SECOND), ZoneOffset.UTC).isAfter(ZonedDateTime.now())) {
            throw new CapabilityNotValidException("Capability token is not yet valid");
        }
        if (ZonedDateTime.of(getExpirationTime().plusSeconds(CAPABILITY_LEEWAY_SECOND), ZoneOffset.UTC).isBefore(ZonedDateTime.now())) {
            throw new CapabilityNotValidException("Capability token is expired");
        }
    }

    public String getSecret() { return secret; }

    public boolean isReadPermission() { return readPermission; }

    public boolean isWritePermission() { return writePermission; }

    public boolean isAppropriatePermission() { return appropriatePermission; }

    public boolean isDownloadPermission() { return downloadPermission; }

    public String getScopeType() { return scopeType; }

    public Album getAlbum() { return album; }

    public void setScopeType(String scopeType) { this.scopeType = scopeType; }

    public void setAlbum(Album album) { this.album = album; }

    public String getSecretBeforeHash() { return secretBeforeHash; }

    public void addMutation(Mutation mutation) { mutations.add(mutation); }

    public static class CapabilityBuilder {


        private LocalDateTime expirationTime;
        private LocalDateTime notBeforeTime;
        private String title;
        private boolean readPermission;
        private boolean writePermission;
        private boolean appropriatePermission;
        private boolean downloadPermission;
        private User user;
        private ScopeType scopeType;
        private Album album;
        private Series series;
        private Study study;

        public CapabilityBuilder () { /*empty*/ }

        public CapabilityBuilder expirationTime (LocalDateTime expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }
        public CapabilityBuilder notBeforeTime (LocalDateTime notBeforeTime) {
            this.notBeforeTime = notBeforeTime;
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
        public CapabilityBuilder appropriatePermission (boolean appropriatePermission) {
            this.appropriatePermission = appropriatePermission;
            return this;
        }
        public CapabilityBuilder downloadPermission (boolean downloadPermission) {
            this.downloadPermission = downloadPermission;
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

        public Capability build() throws CapabilityBadRequestException {
            if (user == null) {
                throw new IllegalStateException("Missing user");
            }
            if (expirationTime == null) {
                throw new IllegalStateException("Missing expirationTime");
            }
            if (notBeforeTime == null) {
                throw new IllegalStateException("Missing notBeforeTime");
            }
            if (title == null) {
                throw new IllegalStateException("Missing title");
            }
            return new Capability(this);
        }
    }
}
