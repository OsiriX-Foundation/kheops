package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.entity.WebhookHistory;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class WebhookHistoryResponse {

    @XmlElement(name = "status")
    private Integer status;
    @XmlElement(name = "is_manual_trigger")
    private boolean isManualTrigger;
    @XmlElement(name = "type")
    private String type;




    private WebhookHistoryResponse() { /*Empty*/ }

    public WebhookHistoryResponse(WebhookHistory webhookHistory) {

        this.status = webhookHistory.getStatus();
        this.isManualTrigger = webhookHistory.getManualTrigger();
        if(webhookHistory.getNewSeries()) {
            type = "new series";
        } else if(webhookHistory.getNewUser()) {
            type = "new user";
        }
    }
}
