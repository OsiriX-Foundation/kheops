package online.kheops.auth_server.entity;

import online.kheops.auth_server.webhook.WebhookType;

import javax.persistence.*;
import java.time.LocalDateTime;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "webhooks_history")
public class WebhookHistory {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "id")
    private String id;

    @Basic(optional = false)
    @Column(name = "status")
    private long status;

    @Basic(optional = false)
    @Column(name = "attempt")
    private long attempt;

    @Basic(optional = false)
    @Column(name = "time", updatable = false)
    private LocalDateTime time;

    @Basic(optional = false)
    @Column(name = "is_manual_trigger", updatable = false)
    private Boolean isManualTrigger;

    @Basic(optional = false)
    @Column(name = "new_series")
    private Boolean newSeries;

    @Basic(optional = false)
    @Column(name = "new_user")
    private Boolean newUser;

    @ManyToOne
    @JoinColumn (name = "webhook_fk", nullable=false, insertable = false, updatable = false)
    private Webhook webhook;

    public WebhookHistory() {}

    public WebhookHistory(String id, long attempt, Integer status, boolean isManualTrigger, WebhookType type, Webhook webhook) {

        this.id = id;
        this.attempt = attempt;
        this.status = status;
        this.time = time.now();
        this.isManualTrigger = isManualTrigger;
        this.webhook = webhook;
        webhook.addWebhookHistory(this);
        if(type.equals(WebhookType.NEW_USER)) {
            this.newUser = true;
            this.newSeries = false;
        } else if (type.equals(WebhookType.NEW_SERIES)) {
            this.newUser = false;
            this.newSeries = true;
        }
    }

    public long getStatus() { return status; }
    public LocalDateTime getTime() { return time; }
    public Boolean getManualTrigger() { return isManualTrigger; }
    public Boolean getNewSeries() { return newSeries; }
    public Boolean getNewUser() { return newUser; }
    public String getId() { return id; }
    public long getAttempt() { return attempt; }
}
