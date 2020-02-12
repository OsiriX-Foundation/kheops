package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.WebhookAttempt;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class WebhookTriggerResponse {


    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "is_manual_trigger")
    private boolean isManualTrigger;
    @XmlElement(name = "event")
    private String event;
    @XmlElement(name = "attempts")
    private List<WebhookAttemptResponse> webhookAttemptResponseList;



    private WebhookTriggerResponse() { /*Empty*/ }

    public WebhookTriggerResponse(WebhookTrigger webhookTrigger) {

        id = webhookTrigger.getId();
        isManualTrigger = webhookTrigger.isManualTrigger();
        if(webhookTrigger.getNewSeries()) {
            event = WebhookType.NEW_SERIES.name().toLowerCase();
        } else if(webhookTrigger.getNewUser()) {
            event = WebhookType.NEW_USER.name().toLowerCase();
        }

        if(!webhookTrigger.getWebhookAttempts().isEmpty()) {
            webhookAttemptResponseList = new ArrayList<>();
            for (WebhookAttempt webhookAttempt : webhookTrigger.getWebhookAttempts()) {
                webhookAttemptResponseList.add(new WebhookAttemptResponse(webhookAttempt));
            }
        }
    }
}
