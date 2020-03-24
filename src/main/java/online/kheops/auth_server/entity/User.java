package online.kheops.auth_server.entity;

import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;
import online.kheops.auth_server.user.IdToken;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@SuppressWarnings("unused")
@Entity
@Table(name = "users")

public class User {
    private static final Logger LOG = Logger.getLogger(User.class.getName());

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "keycloak_id")
    private String keycloakId;

    @Basic()
    @Column(name = "email")
    private String email;

    @Basic()
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "user")
    private Set<Capability> capabilities = new HashSet<>();

    @OrderColumn(name = "pk")
    @OneToMany(mappedBy = "user")
    private Set<AlbumUser> albumUser = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Webhook> webhooks = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "privateTargetUser")
    private Set<Event> privateEvent = new HashSet<>();

    @OneToMany(mappedBy = "toUser")
    private Set<Mutation> mutations = new HashSet<>();

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "inbox_fk", unique = true, nullable = false, updatable = false)
    private Album inbox;

    //@OneToOne(mappedBy = "user_fk",fetch = FetchType.LAZY)
    //private WebhookTrigger webhookTrigger;

    public User() {}

    public User(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public User(IdToken idToken) {
        keycloakId = idToken.getSub();
        email = idToken.getEmail();
        name = idToken.getName();
    }

    public long getPk() {
        return pk;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public String getEmail() {
        return email;
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

    public void addWebhook(Webhook webhook) { this.webhooks.add(webhook); }

    public void setEmail(String email) { this.email = email; }

    public void setName(String name) { this.name = name; }

    public String getName() { return this.name; }


    @Override
    public String toString() {
        return "[User keycloak_id:" + getKeycloakId() + " email:" + getEmail() + "]";
    }
}
