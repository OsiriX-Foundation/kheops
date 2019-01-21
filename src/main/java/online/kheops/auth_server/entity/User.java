package online.kheops.auth_server.entity;

import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponse;

import javax.persistence.*;

import javax.persistence.Table;

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
    @Column(name = "keycloak_id")
    private String keycloakId;

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

    public User() {}

    public User(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public long getPk() {
        return pk;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public String getEmail() {
        try {
            final Keycloak keycloak = new Keycloak();
            final UserResponse userResponse = keycloak.getUser(keycloakId);
            return userResponse.getEmail();
        } catch (UserNotFoundException | KeycloakException e) {
            return "UNKNOWN";//TODO
        }
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
