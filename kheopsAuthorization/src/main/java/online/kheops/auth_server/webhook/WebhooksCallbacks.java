package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.WebhookAttempt;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;

public class WebhooksCallbacks {
    private static final Logger LOG = Logger.getLogger(WebhooksCallbacks.class.getName());


    public WebhooksCallbacks(Response response, WebhookTrigger webhookTrigger, int cnt, WebhookAsyncRequest asyncRequest) {

        cnt--;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            webhookTrigger = em.merge(webhookTrigger);
            final WebhookAttempt webhookAttempt = new WebhookAttempt(response.getStatus(), NUMBER_OF_RETRY_WEBHOOK - cnt, webhookTrigger);
            em.persist(webhookAttempt);
            tx.commit();
        } catch (Exception e) {
            LOG.log(Level.WARNING,"",e);//TODO debug
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        if (!Response.Status.Family.familyOf(response.getStatus()).equals(Response.Status.Family.SUCCESSFUL) && cnt > 0) {
            asyncRequest.retry(cnt);
        }
    }
}
