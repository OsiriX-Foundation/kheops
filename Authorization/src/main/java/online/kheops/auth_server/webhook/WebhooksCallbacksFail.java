package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.WebhookAttempt;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;

public class WebhooksCallbacksFail {
    private static final Logger LOG = Logger.getLogger(WebhooksCallbacksFail.class.getName());


    public WebhooksCallbacksFail(Throwable throwable, WebhookTrigger webhookTrigger, int cnt, WebhookAsyncRequest asyncRequest) {
        cnt--;
        LOG.log(Level.WARNING, "FAIL WEBHOOK url :"+ asyncRequest.getWebhook().getUrl(), throwable);
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            webhookTrigger = em.merge(webhookTrigger);
            final WebhookAttempt webhookAttempt = new WebhookAttempt(-1, NUMBER_OF_RETRY_WEBHOOK - cnt, webhookTrigger);
            em.persist(webhookAttempt);
            tx.commit();
        } catch (Exception e) {
            LOG.log(Level.WARNING,"",e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        if (cnt > 0) {
            asyncRequest.retry(cnt);
        }
    }
}
