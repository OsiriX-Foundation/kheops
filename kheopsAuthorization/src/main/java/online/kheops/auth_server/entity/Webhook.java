package online.kheops.auth_server.entity;

import online.kheops.auth_server.webhook.WebhookPostParameters;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})

@NamedQueries({
        @NamedQuery(name = "Webhook.findById", 
        query = "SELECT w FROM Webhook w WHERE :webhookId = w.id"),
        @NamedQuery(name = "Webhook.findByIdAndAlbum",
        query = "SELECT w FROM Webhook w JOIN w.album a WHERE :webhookId = w.id AND a = :album"),
        @NamedQuery(name = "Webhook.findAllByAlbum",
        query = "SELECT w FROM Webhook w JOIN w.album a WHERE a = :album ORDER BY w.creationTime desc"),
        @NamedQuery(name = "Webhook.findAllByAlbumAndUrl",
        query = "SELECT w FROM Webhook w JOIN w.album a WHERE a = :album AND w.url = :url ORDER BY w.creationTime desc"),
        @NamedQuery(name = "Webhook.countByAlbum",
        query = "SELECT count(w) FROM Webhook w JOIN w.album a WHERE a = :album"),
        @NamedQuery(name = "Webhook.countByAlbumAndUrl",
        query = "SELECT count(w) FROM Webhook w JOIN w.album a WHERE a = :album AND w.url = :url"),
        @NamedQuery(name = "Webhook.findAllEnabledAndForNewSeriesByStudyUID",
        query = "SELECT DISTINCT w FROM Album a JOIN a.albumSeries als JOIN als.series s JOIN s.study st JOIN a.webhooks w WHERE st.studyInstanceUID = :StudyInstanceUID AND w.enabled = true AND w.newSeries = true")
})

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
    private boolean newSeries;

    @Basic(optional = false)
    @Column(name = "remove_series")
    private boolean removeSeries;

    @Basic(optional = false)
    @Column(name = "delete_album")
    private boolean deleteAlbum;

    @Basic(optional = false)
    @Column(name = "new_user")
    private boolean newUser;

    @Basic(optional = false)
    @Column(name = "enabled")
    private boolean enabled;

    @ManyToOne
    @JoinColumn (name = "album_fk", nullable=false, insertable = true, updatable = false)
    private Album album;

    @ManyToOne
    @JoinColumn (name = "user_fk", nullable=false, insertable = true, updatable = false)
    private User user;

    @OneToMany(mappedBy = "webhook")
    @OrderBy("pk DESC")
    private Set<WebhookTrigger> webhookTriggers = new HashSet<>();

    public Webhook() {}

    public Webhook(WebhookPostParameters webhookPostParameters, String id, Album album, User user) {
        this.name = webhookPostParameters.getName();
        this.url = webhookPostParameters.getUrl();
        if(webhookPostParameters.isUseSecret()) {
            this.secret = webhookPostParameters.getSecret();
        } else {
            this.secret = null;
        }
        this.id = id;
        this.secret = webhookPostParameters.getSecret();
        this.newSeries = webhookPostParameters.isNewSeries();
        this.removeSeries = webhookPostParameters.isRemoveSeries();
        this.newUser = webhookPostParameters.isNewUser();
        this.deleteAlbum = webhookPostParameters.isDeleteAlbum();
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

    public boolean getRemoveSeries() { return removeSeries; }

    public boolean getNewUser() {
        return newUser;
    }

    public boolean getDeleteAlbum() { return deleteAlbum; }

    public boolean isEnabled() { return enabled; }

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

    public void setNewSeries(boolean newSeries) {
        this.newSeries = newSeries;
    }

    public void setRemoveSeries(boolean removeSeries) { this.removeSeries = removeSeries; }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public void setDeleteAlbum(boolean deleteAlbum) { this.deleteAlbum = deleteAlbum; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getPk() { return pk; }
}
