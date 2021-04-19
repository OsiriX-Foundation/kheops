package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.WebhookAttempt;

import javax.xml.bind.annotation.XmlElement;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class WebhookAttemptResponse {

    @XmlElement(name = "status")
    private Long status;
    @XmlElement(name = "time")
    private String time;
    @XmlElement(name = "attempt")
    private Long attempt;

    private WebhookAttemptResponse() { /*Empty*/ }

    public WebhookAttemptResponse(WebhookAttempt webhookAttempt) {
        time = ZonedDateTime.of(webhookAttempt.getTime(), ZoneOffset.UTC).toString();
        status = webhookAttempt.getStatus();
        attempt = webhookAttempt.getAttempt();
    }
}

