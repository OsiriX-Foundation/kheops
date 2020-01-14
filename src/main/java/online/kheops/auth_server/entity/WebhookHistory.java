package online.kheops.auth_server.entity;

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
    @Column(name = "status")
    private Integer status;

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

}
