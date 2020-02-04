package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.WebhookAttempt;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class WebhookAttemptResponse {

    @XmlElement(name = "status")
    private Long status;
    @XmlElement(name = "time")
    private LocalDateTime time;
    @XmlElement(name = "attempt")
    private Long attempt;

    private WebhookAttemptResponse() { /*Empty*/ }

    public WebhookAttemptResponse(WebhookAttempt webhookAttempt) {
        time = webhookAttempt.getTime();
        status = webhookAttempt.getStatus();
        attempt = webhookAttempt.getAttempt();
    }
}

