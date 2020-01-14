package online.kheops.auth_server.entity;

import online.kheops.auth_server.webhook.WebhookId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "webhooks")
public class Webhook {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "id")
    private String id;

    @Basic(optional = false)
    @Column(name = "name")
    private String name;

    @Column(name = "secret")
    private String secret;

    @Basic(optional = false)
    @Column(name = "url")
    private String url;

    @Basic(optional = false)
    @Column(name = "new_series")
    private Boolean newSeries;

    @Basic(optional = false)
    @Column(name = "new_user")
    private Boolean newUser;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = false, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "user_fk", nullable=false, insertable = false, updatable = false)
    private User user;

    @OneToMany
    @JoinColumn (name = "webhook_fk", nullable = false)
    private Set<WebhookHistory> webhookHistory  = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        id = new WebhookId().getId();
    }

    public Webhook() {}

    public Webhook(String name, String url, Album album, User user, String secret, boolean newSeries, boolean newUser) {
        this.name = name;
        this.url = url;
        this.secret = secret;
        this.newSeries = newSeries;
        this.newUser = newUser;
        this.album = album;
        this.user = user;

        album.addWebhook(this);
        user.addWebhook(this);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSecret() {
        return secret;
    }

    public boolean useSecret() {
        return secret != null;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getNewSeries() {
        return newSeries;
    }

    public Boolean getNewUser() {
        return newUser;
    }

    public Album getAlbum() {
        return album;
    }

    public User getUser() {
        return user;
    }

    public Set<WebhookHistory> getWebhookHistory() {
        return webhookHistory;
    }
}
