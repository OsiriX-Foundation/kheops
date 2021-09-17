package online.kheops.auth_server.entity;

import javax.persistence.*;

import static online.kheops.auth_server.util.JPANamedQueryConstants.*;

@NamedQueries({
        @NamedQuery(name = "WebhookTriggerSeries.deleteAllWebhookTriggerSeriesBySeries",
                query = "DELETE FROM WebhookTriggerSeries wt WHERE wt.series = :"+SERIES)
})

@Entity
@Table(name = "webhook_trigger_series")
public class WebhookTriggerSeries {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    @ManyToOne
    @JoinColumn (name = "webhook_trigger_fk", nullable=false, insertable = true, updatable = false)
    private WebhookTrigger webhookTrigger;

    @ManyToOne
    @JoinColumn (name = "series_fk", nullable=false, insertable = true, updatable = false)
    private Series series;

    public WebhookTriggerSeries() {}

    public WebhookTriggerSeries(WebhookTrigger webhookTrigger, Series series) {
        this.webhookTrigger = webhookTrigger;
        this.series = series;
    }
}
