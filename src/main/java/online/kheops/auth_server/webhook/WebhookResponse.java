package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.entity.WebhookHistory;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class WebhookResponse {

    @XmlElement(name = "id")
    private String id;

    @XmlElement(name = "url")
    private String url;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "use_secret")
    private boolean useSecret;
    @XmlElement(name = "new_series")
    private boolean newSeries;
    @XmlElement(name = "new_user")
    private boolean newUser;
    @XmlElement(name = "enable")
    private boolean enable;
    @XmlElement(name = "last_history")
    private List<String> history;
    @XmlElement(name = "number_of_history")
    private Integer numberOfHistory;
    @XmlElement(name = "history")
    private List<WebhookHistoryResponse> fullHistory;

    private WebhookResponse() { /*Empty*/ }

    public WebhookResponse(Webhook webhook) {

        this.id = webhook.getId();
        this.name = webhook.getName();
        this.url = webhook.getUrl();
        this.useSecret = webhook.useSecret();
        this.newSeries = webhook.getNewSeries();
        this.newUser = webhook.getNewUser();
        this.enable = webhook.isEnable();
        this.numberOfHistory = webhook.getWebhookHistory().size();

        history = new ArrayList<>();
    }

    public void addHistory(WebhookHistory webhookHistory) {
        if(webhookHistory.getStatus() >= 200 && webhookHistory.getStatus() <=299) {
            this.history.add("Pass");
        } else {
            this.history.add("Fail");
        }
    }

    public void addFullHistory(WebhookHistory webhookHistory) {
        if (fullHistory == null) {
            fullHistory = new ArrayList<>();
        }
        this.fullHistory.add(new WebhookHistoryResponse(webhookHistory));
    }
}
