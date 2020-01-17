package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.WebhookHistory;

import javax.xml.bind.annotation.XmlElement;

public class WebhookHistoryResponse {

    @XmlElement(name = "status")
    private long status;
    @XmlElement(name = "is_manual_trigger")
    private boolean isManualTrigger;
    @XmlElement(name = "type")
    private String type;




    private WebhookHistoryResponse() { /*Empty*/ }

    public WebhookHistoryResponse(WebhookHistory webhookHistory) {

        this.status = webhookHistory.getStatus();
        this.isManualTrigger = webhookHistory.getManualTrigger();
        if(webhookHistory.getNewSeries()) {
            type = WebhookTypes.NEW_SERIES.name();
        } else if(webhookHistory.getNewUser()) {
            type = WebhookTypes.NEW_USER.name();
        }
    }
}
