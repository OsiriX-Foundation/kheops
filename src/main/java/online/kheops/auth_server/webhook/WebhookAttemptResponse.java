package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.WebhookAttempt;

import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDateTime;

public class WebhookAttemptResponse implements Comparable<WebhookAttemptResponse> {

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof WebhookAttemptResponse) {
            final WebhookAttemptResponse webhookAttemptResponse = (WebhookAttemptResponse) obj;
            return  webhookAttemptResponse.status == status &&
                    webhookAttemptResponse.attempt == attempt &&
                    webhookAttemptResponse.time.compareTo(time) == 0;
        }
        return false;
    }

    @Override
    public int compareTo(WebhookAttemptResponse webhookAttemptResponse) {
        return attempt.compareTo(webhookAttemptResponse.attempt);
    }

    private int hashCode;
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = attempt.hashCode();
            result = 31 * result + status.hashCode();
            result = 31 * result + time.hashCode();
            hashCode = result;
        }
        return result;
    }
}

