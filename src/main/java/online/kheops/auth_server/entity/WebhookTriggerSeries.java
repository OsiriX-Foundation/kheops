package online.kheops.auth_server.entity;


import javax.persistence.*;

@SuppressWarnings({"WeakerAccess", "unused"})
@Entity
@Table(name = "webhook_trigger_series")
public class WebhookTriggerSeries {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @ManyToOne
    @JoinColumn (name = "webhook_trigger_fk", nullable=false, insertable = false, updatable = false)
    private WebhookTrigger webhookTrigger;

    @ManyToOne
    @JoinColumn (name = "series_fk", nullable=false, insertable = false, updatable = false)
    private Series series;

    public WebhookTriggerSeries() {}

    public WebhookTriggerSeries(WebhookTrigger webhookTrigger, Series series) {
        this.webhookTrigger = webhookTrigger;
        this.series = series;
        series.addWebHookTriggerSeries(this);
        webhookTrigger.addWebHookTriggerSeries(this);
    }

    public Series getSeries() {
        return series;
    }
}
