package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.WebhookHistory;

import javax.xml.bind.annotation.XmlElement;

public class WebhookHistoryResponse {


    @XmlElement(name = "id")
    private String id;
    @XmlElement(name = "attempt")
    private long attempt;
    @XmlElement(name = "status")
    private long status;
    @XmlElement(name = "is_manual_trigger")
    private boolean isManualTrigger;
    @XmlElement(name = "type")
    private String type;


    private WebhookHistoryResponse() { /*Empty*/ }

    public WebhookHistoryResponse(WebhookHistory webhookHistory) {

        this.id = webhookHistory.getId();
        this.attempt = webhookHistory.getAttempt();
        this.status = webhookHistory.getStatus();
        this.isManualTrigger = webhookHistory.getManualTrigger();
        if(webhookHistory.getNewSeries()) {
            type = WebhookType.NEW_SERIES.name();
        } else if(webhookHistory.getNewUser()) {
            type = WebhookType.NEW_USER.name();
        }
    }
}
