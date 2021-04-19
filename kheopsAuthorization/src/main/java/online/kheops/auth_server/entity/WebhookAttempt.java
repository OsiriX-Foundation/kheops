package online.kheops.auth_server.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_attempts")
public class WebhookAttempt {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @Basic(optional = false)
    @Column(name = "status")
    private long status;

    @Basic(optional = false)
    @Column(name = "attempt")
    private long attempt;

    @Basic(optional = false)
    @Column(name = "time", updatable = false)
    private LocalDateTime time;

    @ManyToOne
    @JoinColumn (name = "webhook_trigger_fk", nullable=false, insertable = true, updatable = false)
    private WebhookTrigger webhookTrigger;

    public WebhookAttempt() {}

    public WebhookAttempt(int status, int attempt, WebhookTrigger webhookTrigger) {
        this.status = status;
        this.attempt = attempt;
        time = LocalDateTime.now();
        this.webhookTrigger = webhookTrigger;
        webhookTrigger.addWebhookAttempt(this);
    }

    public long getStatus() { return status; }
    public long getAttempt() { return attempt; }
    public LocalDateTime getTime() { return time; }
    public WebhookTrigger getWebhookTrigger() { return webhookTrigger; }
}
