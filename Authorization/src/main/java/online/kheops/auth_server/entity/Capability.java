package online.kheops.auth_server.entity;

import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.capability.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static online.kheops.auth_server.capability.CapabilityToken.hashCapability;
import static online.kheops.auth_server.util.Consts.CAPABILITY_LEEWAY_SECOND;
import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

@SuppressWarnings("unused")

@NamedQueries({
        @NamedQuery(name = "Capability.findBySecret",
        query = "SELECT c FROM Capability c WHERE c.secret = :"+SECRET),
        @NamedQuery(name = "Capability.findByIdAndUser",
        query = "SELECT c FROM Capability c LEFT JOIN c.album a LEFT JOIN a.albumUser au WHERE ((:"+USER+" = au.user AND au.admin = true) OR (:"+USER+" = c.user)) AND :"+CAPABILITY_ID+" = c.id"),
        @NamedQuery(name = "Capability.findByIdAndAlbumId",
        query = "SELECT c FROM Capability c LEFT JOIN c.album a WHERE a.id = :"+ALBUM_ID+" AND :"+CAPABILITY_ID+" = c.id"),
        @NamedQuery(name = "Capability.findById",
        query = "SELECT c FROM Capability c WHERE :"+CAPABILITY_ID+" = c.id"),
        @NamedQuery(name = "Capability.findAllByUser",
        query = "SELECT c FROM Capability c WHERE :"+USER+" = c.user ORDER BY c.issuedAtTime desc"),
        @NamedQuery(name = "Capability.findAllValidByUser",
        query = "SELECT c FROM Capability c WHERE :"+USER+" = c.user AND c.revokedTime = null AND c.expirationTime > :"+DATE_TIME_NOW+"  ORDER BY c.issuedAtTime desc"),
        @NamedQuery(name = "Capability.findAllByAlbum",
        query = "SELECT c FROM Capability c WHERE :"+ALBUM_ID+" = c.album.id order by c.issuedAtTime desc"),
        @NamedQuery(name = "Capability.findAllValidByAlbum",
        query = "SELECT c FROM Capability c WHERE :"+ALBUM_ID+" = c.album.id AND c.revokedTime = null AND c.expirationTime > :"+DATE_TIME_NOW+" ORDER BY c.issuedAtTime desc"),
        @NamedQuery(name = "Capability.countAllByUser",
        query = "SELECT count(c) FROM Capability c WHERE :"+USER+" = c.user"),
        @NamedQuery(name = "Capability.countAllValidByUser",
        query = "SELECT count(c) FROM Capability c WHERE :"+USER+" = c.user AND c.revokedTime = null AND c.expirationTime > :"+DATE_TIME_NOW+""),
        @NamedQuery(name = "Capability.countAllByAlbum",
        query = "SELECT count(c) FROM Capability c WHERE :"+ALBUM_ID+" = c.album.id"),
        @NamedQuery(name = "Capability.countAllValidByAlbum",
        query = "SELECT count(c) FROM Capability c WHERE :"+ALBUM_ID+" = c.album.id AND c.revokedTime = null AND c.expirationTime > :"+DATE_TIME_NOW+""),
        @NamedQuery(name = "Capability.deleteAllByAlbum",
        query = "DELETE FROM Capability c WHERE c.album = :"+ALBUM)
})

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
    @Column(name = "read_permission", updatable = false)
    private boolean readPermission ;

    @Basic
    @Column(name = "appropriate_permission", updatable = false)
    private boolean appropriatePermission ;

    @Basic
    @Column(name = "download_permission", updatable = false)
    private boolean downloadPermission ;

    @Basic(optional = false)
    @Column(name = "write_permission", updatable = false)
    private boolean writePermission ;

    @ManyToOne
    @JoinColumn(name = "user_fk", insertable = true, updatable=false)
    private User user;

    @Basic(optional = false)
    @Column(name = "scope_type", updatable = false)
    @Enumerated(value = EnumType.STRING)
    private ScopeType scopeType ;

    @ManyToOne
    @JoinColumn(name = "album_fk", insertable = true, updatable=false)
    private Album album;

    @ManyToOne
    @JoinColumn(name = "revoked_by_user_fk", insertable = true, updatable=true)
    private User revokedByUser;

    @PrePersist
    public void onPrePersist() {
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        issuedAtTime = now;
        updatedTime = now;
        if(notBeforeTime == null) {
            notBeforeTime = now;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capability capability = (Capability) o;
        return id.equals(capability.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    public Capability() {}

    private Capability(CapabilityBuilder builder) throws BadQueryParametersException {
        this.secretBeforeHash = builder.secretBeforeHash;
        this.secret = hashCapability(secretBeforeHash);
        this.expirationTime = builder.expirationTime;
        this.notBeforeTime = builder.notBeforeTime;
        this.title  = builder.title;
        this.id = builder.id;
        this.user = builder.user;
        builder.scopeType.setCapabilityEntityScope(this, builder.album, builder.study, builder.series);
        this.readPermission = builder.readPermission;
        this.writePermission = builder.writePermission;
        this.appropriatePermission = builder.appropriatePermission;
        this.downloadPermission = builder.downloadPermission;
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

    public String getTitle() { return title; }

    public String getId() { return id; }

    public void setTitle(String description) { this.title = description; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public User getRevokedByUser() { return revokedByUser; }

    public void setRevokedByUser(User revokedByUser) {

        if (this.revokedTime != null) {
            throw new IllegalStateException("Can't unrevoke a revoked capability");
        }
        this.revokedByUser = revokedByUser;
        this.revokedTime = LocalDateTime.now(ZoneOffset.UTC);

    }

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

    public boolean hasReadPermission() { return readPermission; }

    public boolean hasWritePermission() { return writePermission; }

    public boolean hasAppropriatePermission() { return appropriatePermission; }

    public boolean hasDownloadButtonPermission() { return downloadPermission; }

    public ScopeType getScopeType() { return scopeType; }

    public Album getAlbum() { return album; }

    public void setScopeType(ScopeType scopeType) { this.scopeType = scopeType; }

    public void setAlbum(Album album) { this.album = album; }

    public String getSecretBeforeHash() { return secretBeforeHash; }

    public static class CapabilityBuilder {

        private String secretBeforeHash;
        private LocalDateTime expirationTime;
        private LocalDateTime notBeforeTime;
        private String title;
        private String id;
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
        public CapabilityBuilder id (String id) {
            this.id = id;
            return this;
        }
        public CapabilityBuilder secretBeforeHash (String secretBeforeHash) {
            this.secretBeforeHash = secretBeforeHash;
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

        public Capability build() throws BadQueryParametersException {
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
            if (secretBeforeHash == null) {
                throw new IllegalStateException("Missing secretBeforeHash");
            }
            return new Capability(this);
        }
    }
}
