package online.kheops.auth_server.entity;

import online.kheops.auth_server.webhook.WebhookType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static online.kheops.auth_server.util.JPANamedQueryConstants.WEBHOOK_TRIGGER_ID;

@SuppressWarnings({"WeakerAccess", "unused"})

@NamedQueries({
        @NamedQuery(name = "WebhookTrigger.findById",
                query = "SELECT w FROM WebhookTrigger w WHERE :"+WEBHOOK_TRIGGER_ID+" = w.id")
})

@Entity
@Table(name = "webhook_triggers")
public class WebhookTrigger {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "id")
    private String id;

    @Basic(optional = false)
    @Column(name = "is_manual_trigger", updatable = false)
    private boolean isManualTrigger;

    @Basic(optional = false)
    @Column(name = "new_series")
    private boolean newSeries;

    @Basic(optional = false)
    @Column(name = "remove_series")
    private boolean removeSeries;

    @Basic(optional = false)
    @Column(name = "new_user")
    private boolean newUser;

    @OneToMany(mappedBy = "webhookTrigger")
    @OrderBy("attempt desc")
    private Set<WebhookAttempt> webhookAttempts = new HashSet<>();

    @ManyToMany()
    @JoinTable(name = "webhook_trigger_series",
            joinColumns = @JoinColumn(name = "webhook_trigger_fk"),
            inverseJoinColumns = @JoinColumn(name = "series_fk"))
    private Set<Series> series = new HashSet<>();

    @ManyToOne
    @JoinColumn (name = "webhook_fk", nullable=false, insertable = true, updatable = false)
    private Webhook webhook;

    @OneToOne
    @JoinColumn(name = "user_fk", unique = false, nullable = true, updatable = false)
    private User user;


    public WebhookTrigger() {}

    public WebhookTrigger(String id, boolean isManualTrigger, WebhookType type, Webhook webhook) {

        this.id = id;
        this.isManualTrigger = isManualTrigger;
        this.webhook = webhook;
        webhook.addWebhookTrigger(this);
        if(type.equals(WebhookType.NEW_USER)) {
            this.newUser = true;
            this.newSeries = false;
            this.removeSeries = false;
        } else if (type.equals(WebhookType.NEW_SERIES)) {
            this.newUser = false;
            this.newSeries = true;
            this.removeSeries = false;
        } else if (type.equals(WebhookType.REMOVE_SERIES)) {
            this.newUser = false;
            this.newSeries = false;
            this.removeSeries = true;
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {return user; }

    public void addWebhookAttempt(WebhookAttempt webhookAttempt) { this.webhookAttempts.add(webhookAttempt); }

    public boolean isManualTrigger() { return isManualTrigger; }
    public boolean getNewSeries() { return newSeries; }
    public boolean getRemoveSeries() { return removeSeries; }
    public boolean getNewUser() { return newUser; }
    public String getId() { return id; }
    public long getPk() { return pk; }

    public Set<WebhookAttempt> getWebhookAttempts() { return webhookAttempts; }

    public boolean isSuccess() {
        for (WebhookAttempt webhookAttempt: webhookAttempts) {
            if (webhookAttempt.getStatus() >= 200 && webhookAttempt.getStatus() <= 299) {
                return true;
            }
        }
        return false;
    }

    public void addSeries(Series series) {
        this.series.add(series);
    }

    public void removeSeries(Series series) {
        this.series.remove(series);
    }

    public void removeAllSeries() {
        this.series.clear();
    }

    public Set<Series> getSeries() { return series; }

    public Webhook getWebhook() {
        return webhook;
    }
}
