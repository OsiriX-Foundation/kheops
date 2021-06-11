package online.kheops.auth_server.webhook;

import javax.persistence.EntityManager;
import java.security.SecureRandom;
import java.util.Random;

import static online.kheops.auth_server.webhook.Webhooks.webhookTriggerExist;

public class WebhookRequestId {

    private String requestId;

    private static final String DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int ID_LENGTH = 30;
    public static final String ID_PATTERN = "[A-Za-z0-9]{" + ID_LENGTH + "}";
    private static final Random rdm = new SecureRandom();

    public WebhookRequestId(EntityManager em) {
        StringBuilder idBuilder = new StringBuilder();

        do {
            idBuilder.setLength(0);
            while (idBuilder.length() < ID_LENGTH) {
                int index = rdm.nextInt(DICT.length());
                idBuilder.append(DICT.charAt(index));
            }
        } while (webhookTriggerExist(idBuilder.toString(), em));
        requestId = idBuilder.toString();
    }

    public String getRequestId() {
        return requestId;
    }
}
