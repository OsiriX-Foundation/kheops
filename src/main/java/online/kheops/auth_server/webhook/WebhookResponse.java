package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;

import javax.xml.bind.annotation.XmlElement;

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

    private WebhookResponse() { /*Empty*/ }

    public WebhookResponse(Webhook webhook) {

        this.id = webhook.getId();
        this.name = webhook.getName();
        this.url = webhook.getUrl();
        this.useSecret = webhook.useSecret();
        this.newSeries = webhook.getNewSeries();
        this.newUser = webhook.getNewUser();
    }
}
