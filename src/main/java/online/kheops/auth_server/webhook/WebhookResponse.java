package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.entity.WebhookAttempt;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    private List<String> lastTriggers;
    @XmlElement(name = "number_of_triggers")
    private Integer numberOfTriggers;
    @XmlElement(name = "triggers")
    private List<WebhookTriggerResponse> fullTriggers;

    private WebhookResponse() { /*Empty*/ }

    public WebhookResponse(Webhook webhook) {

        id = webhook.getId();
        name = webhook.getName();
        url = webhook.getUrl();
        useSecret = webhook.useSecret();
        events = new ArrayList<>();
        if(webhook.getNewSeries()) {
            events.add(WebhookType.NEW_SERIES.name().toLowerCase());
        }
        if(webhook.getNewUser()) {
            events.add(WebhookType.NEW_USER.name().toLowerCase());
        }

        enabled = webhook.isEnabled();
        numberOfTriggers = webhook.getWebhookTriggers().size();
    }

    public void addTrigger(WebhookTrigger webhookTrigger) {
        if (lastTriggers == null) {
            lastTriggers = new ArrayList<>();
        }
        if(webhookTrigger.isSuccess()) {
            this.lastTriggers.add("pass");
        } else {
            this.lastTriggers.add("fail");
        }
    }

    public void addFullTriggers(WebhookTrigger webhookTrigger) {
        if (fullTriggers == null) {
            fullTriggers = new ArrayList<>();
        }
        this.fullTriggers.add(new WebhookTriggerResponse(webhookTrigger));
    }

    public void sortFullTrigers() {
        Collections.sort(fullTriggers);
    }
}
