package online.kheops.auth_server.webhook;

import javax.persistence.EntityManager;
import java.security.SecureRandom;
import java.util.Random;

import static online.kheops.auth_server.webhook.Webhooks.webhookExist;

public class WebhookId {

    private String id;

    private static final String DICT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int ID_LENGTH = 20;
    public static final String ID_PATTERN = "[A-Za-z0-9]{" + ID_LENGTH + "}";
    private static final Random rdm = new SecureRandom();

    public WebhookId(EntityManager em) {
        StringBuilder idBuilder = new StringBuilder();

        do {
            idBuilder.setLength(0);
            while (idBuilder.length() < ID_LENGTH) {
                int index = rdm.nextInt(DICT.length());
                idBuilder.append(DICT.charAt(index));
            }
        } while (webhookExist(idBuilder.toString(), em));
        id = idBuilder.toString();
    }

    public String getId() {
        return id;
    }
}
