package online.kheops.auth_server.entity;

import online.kheops.auth_server.webhook.WebhookId;
import online.kheops.auth_server.webhook.WebhookPostParameters;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @Basic(optional = false)
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

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

    @Basic(optional = false)
    @Column(name = "enabled")
    private Boolean enabled;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = false, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "user_fk", nullable=false, insertable = false, updatable = false)
    private User user;

    @OneToMany
    @JoinColumn (name = "webhook_fk", nullable = false)
    @OrderBy("pk DESC")
    private Set<WebhookTrigger> webhookTriggers = new HashSet<>();

    @PrePersist
    public void onPrePersist() {
        id = new WebhookId().getId();
    }

    private Webhook() {}

    public Webhook(WebhookPostParameters webhookPostParameters, Album album, User user) {
        this.name = webhookPostParameters.getName();
        this.url = webhookPostParameters.getUrl();
        if(webhookPostParameters.isUseSecret()) {
            this.secret = webhookPostParameters.getSecret();
        } else {
            this.secret = null;
        }
        this.secret = webhookPostParameters.getSecret();
        this.newSeries = webhookPostParameters.isNewSeries();
        this.newUser = webhookPostParameters.isNewUser();
        this.album = album;
        this.user = user;
        this.enabled = webhookPostParameters.isEnabled();
        creationTime = LocalDateTime.now();

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

    public boolean getNewSeries() { return newSeries; }

    public boolean getNewUser() {
        return newUser;
    }

    public Boolean isEnabled() { return enabled; }

    public Album getAlbum() {
        return album;
    }

    public User getUser() {
        return user;
    }



    public Set<WebhookTrigger> getWebhookTriggers() {
        return webhookTriggers;
    }

    public void addWebhookTrigger(WebhookTrigger webhookTrigger) {
        this.webhookTriggers.add(webhookTrigger);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setNewSeries(Boolean newSeries) {
        this.newSeries = newSeries;
    }

    public void setNewUser(Boolean newUser) {
        this.newUser = newUser;
    }

    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public long getPk() { return pk; }
}
