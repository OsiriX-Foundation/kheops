package online.kheops.auth_server.entity;

import online.kheops.auth_server.webhook.WebhookRequestId;
import online.kheops.auth_server.webhook.WebhookType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"WeakerAccess", "unused"})

@NamedQueries({
        @NamedQuery(name = "WebhookTrigger.findById",
                query = "SELECT w FROM WebhookTrigger w WHERE :webhookTriggerId = w.id")
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
    private Boolean isManualTrigger;

    @Basic(optional = false)
    @Column(name = "new_series")
    private Boolean newSeries;

    @Basic(optional = false)
    @Column(name = "new_user")
    private Boolean newUser;

    @OneToMany(mappedBy = "webhookTrigger")
    @OrderBy("attempt desc")
    private Set<WebhookAttempt> webhookAttempts = new HashSet<>();

    @OneToMany(mappedBy = "webhookTrigger")
    private Set<WebhookTriggerSeries> webhookTriggersSeries = new HashSet<>();

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
        } else if (type.equals(WebhookType.NEW_SERIES)) {
            this.newUser = false;
            this.newSeries = true;
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {return user; }

    public void addWebhookAttempt(WebhookAttempt webhookAttempt) { this.webhookAttempts.add(webhookAttempt); }

    public Boolean isManualTrigger() { return isManualTrigger; }
    public Boolean getNewSeries() { return newSeries; }
    public Boolean getNewUser() { return newUser; }
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

    public void addWebHookTriggerSeries(WebhookTriggerSeries webhookTriggerSeries) { this.webhookTriggersSeries.add(webhookTriggerSeries); }

    public Set<WebhookTriggerSeries> getWebhookTriggersSeries() {
        return webhookTriggersSeries;
    }

    public Webhook getWebhook() {
        return webhook;
    }
}
