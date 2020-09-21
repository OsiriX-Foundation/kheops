package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.WebhookAttempt;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;

public class WebhooksCallbacksFailDeleteAlbum {
    private static final Logger LOG = Logger.getLogger(WebhooksCallbacksFailDeleteAlbum.class.getName());


    public WebhooksCallbacksFailDeleteAlbum(Throwable throwable, int cnt, WebhookAsyncRequestDeleteAlbum asyncRequest) {
        cnt--;
        LOG.log(Level.WARNING, "", throwable);

        if (cnt > 0) {
            asyncRequest.retry(cnt);
        }
    }
}
