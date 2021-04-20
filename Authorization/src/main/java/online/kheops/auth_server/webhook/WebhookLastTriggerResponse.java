package online.kheops.auth_server.webhook;


import online.kheops.auth_server.entity.WebhookTrigger;

import javax.xml.bind.annotation.XmlElement;

public class WebhookLastTriggerResponse {
    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "status")
    private String status;

    private WebhookLastTriggerResponse() { /*Empty*/ }

    public WebhookLastTriggerResponse(WebhookTrigger webhookTrigger) {
        this.id = webhookTrigger.getId();
        if(webhookTrigger.isSuccess()) {
            this.status = "pass";
        } else {
            this.status = "fail";
        }
    }
}
