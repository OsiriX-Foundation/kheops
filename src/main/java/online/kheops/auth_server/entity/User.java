package online.kheops.auth_server.entity;

import javax.persistence.*;

import javax.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("unused")
@Entity
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Basic(optional = false)
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Basic(optional = false)
    @Column(name = "google_id")
    private String googleId;

    @Basic(optional = false)
    @Column(name = "google_email")
    private String googleEmail;

    @OneToMany
    @JoinColumn (name = "user_fk", nullable=false)
    private Set<Capability> capabilities = new HashSet<>();

    @OneToMany
    @JoinColumn (name = "user_fk", nullable=false)
    private Set<AlbumUser> albumUser = new HashSet<>();

    @OneToMany
    @JoinColumn (name = "user_fk", nullable=false)
    private Set<Event> events = new HashSet<>();

    @OneToMany
    @JoinColumn (name = "private_target_user_fk", nullable=true)
    private Set<Event> privateEvent = new HashSet<>();

    @OneToMany
    @JoinColumn (name = "to_user_fk", nullable=true)
    private Set<Mutation> mutations = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "inbox_fk", unique = true, nullable = false, updatable = false)
    private Album inbox;

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

    public User() {}

    public User(String googleId, String googleEmail) {
        this.googleId = googleId;
        this.googleEmail = googleEmail;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public long getPk() {
        return pk;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    public Set<AlbumUser> getAlbumUser() {
        return albumUser;
    }

    public void addAlbumUser(AlbumUser albumUser) { this.albumUser.add(albumUser); }

    public Set<Event> getEvents() { return events; }

    public void setEvents(Set<Event> events) { this.events = events; }

    public void addEvents(Event event) { this.events.add(event); }

    public Set<Event> getComments() { return privateEvent; }

    public void setComments(Set<Event> comments) { this.privateEvent = comments; }

    public Set<Mutation> getMutations() { return mutations; }

    public void addMutation(Mutation mutation) { this.mutations.add(mutation); }

    public void setMutations(Set<Mutation> mutations) { this.mutations = mutations; }

    public Album getInbox() { return inbox; }

    public void setInbox(Album inbox) { this.inbox = inbox; }
}
