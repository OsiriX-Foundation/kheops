package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.WebhookAttempt;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebhookTriggerResponse implements Comparable<WebhookTriggerResponse> {


    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "is_manual_trigger")
    private boolean isManualTrigger;
    @XmlElement(name = "type")
    private String type;
    @XmlElement(name = "attempts")
    private List<WebhookAttemptResponse> webhookAttemptResponseList;

    private Long pk;


    private WebhookTriggerResponse() { /*Empty*/ }

    public WebhookTriggerResponse(WebhookTrigger webhookTrigger) {

        id = webhookTrigger.getId();
        isManualTrigger = webhookTrigger.isManualTrigger();
        if(webhookTrigger.getNewSeries()) {
            type = WebhookType.NEW_SERIES.name().toLowerCase();
        } else if(webhookTrigger.getNewUser()) {
            type = WebhookType.NEW_USER.name().toLowerCase();
        }

        if(!webhookTrigger.getWebhookAttempts().isEmpty()) {
            webhookAttemptResponseList = new ArrayList<>();
            for (WebhookAttempt webhookAttempt : webhookTrigger.getWebhookAttempts()) {
                webhookAttemptResponseList.add(new WebhookAttemptResponse(webhookAttempt));
            }
            Collections.sort(webhookAttemptResponseList);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof WebhookTriggerResponse) {
            final WebhookTriggerResponse webhookTriggerResponse = (WebhookTriggerResponse) obj;
            return  webhookTriggerResponse.id.compareTo(id) == 0 &&
                    webhookTriggerResponse.isManualTrigger == isManualTrigger &&
                    webhookTriggerResponse.type.compareTo(type) == 0;
        }
        return false;
    }

    @Override
    public int compareTo(WebhookTriggerResponse webhookTriggerResponse) {
        return pk.compareTo(webhookTriggerResponse.pk);
    }

    private int hashCode;
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = id.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + Boolean.valueOf(isManualTrigger).hashCode();
            hashCode = result;
        }
        return result;
    }
}
