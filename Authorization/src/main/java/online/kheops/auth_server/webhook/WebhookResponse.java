package online.kheops.auth_server.webhook;

import online.kheops.auth_server.KheopsInstance;
import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.webhook.WebhookQueries.getNumberOfWebhookTrigger;

public class WebhookResponse {

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "url")
    private String url;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "use_secret")
    private boolean useSecret;
    @XmlElement(name = "events")
    private List<String> events;
    @XmlElement(name = "enabled")
    private boolean enabled;
    @XmlElement(name = "last_triggers")
    private List<WebhookLastTriggerResponse> lastTriggers;
    @XmlElement(name = "number_of_triggers")
    private Long numberOfTriggers;
    @XmlElement(name = "triggers")
    private List<WebhookTriggerResponse> fullTriggers;

    private WebhookResponse() { /*Empty*/ }

    public WebhookResponse(Webhook webhook, EntityManager em) {

        id = webhook.getId();
        name = webhook.getName();
        url = webhook.getUrl();
        useSecret = webhook.useSecret();
        events = new ArrayList<>();
        if(webhook.getNewSeries()) {
            events.add(WebhookType.NEW_SERIES.name().toLowerCase());
        }
        if(webhook.getRemoveSeries()) {
            events.add(WebhookType.REMOVE_SERIES.name().toLowerCase());
        }
        if(webhook.getNewUser()) {
            events.add(WebhookType.NEW_USER.name().toLowerCase());
        }
        if(webhook.getDeleteAlbum()) {
            events.add(WebhookType.DELETE_ALBUM.name().toLowerCase());
        }

        enabled = webhook.isEnabled();
        numberOfTriggers = getNumberOfWebhookTrigger(webhook, em);
    }

    public void addTrigger(WebhookTrigger webhookTrigger) {
        if (lastTriggers == null) {
            lastTriggers = new ArrayList<>();
        }
        this.lastTriggers.add(new WebhookLastTriggerResponse(webhookTrigger));

    }

    public void addFullTriggers(WebhookTrigger webhookTrigger, KheopsInstance kheopsInstance) {
        if (fullTriggers == null) {
            fullTriggers = new ArrayList<>();
        }
        this.fullTriggers.add(new WebhookTriggerResponse(webhookTrigger, kheopsInstance));
    }
}
