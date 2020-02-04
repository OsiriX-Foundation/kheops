package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.entity.WebhookAttempt;
import online.kheops.auth_server.entity.WebhookTrigger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;

public class WebhooksCallbacks implements InvocationCallback<Response> {
    private static final Logger LOG = Logger.getLogger(WebhooksCallbacks.class.getName());

    private WebhookTrigger webhookTrigger;
    private int cnt;
    private WebhookAsyncRequest asyncRequest;

    public WebhooksCallbacks(WebhookTrigger webhookTrigger, int cnt, WebhookAsyncRequest asyncRequest) {
        this.webhookTrigger = webhookTrigger;
        this.cnt = cnt;
        this.asyncRequest = asyncRequest;
    }

    @Override
    public void completed(Response response) {

        cnt--;

        if ((response.getStatus() >= 200 && response.getStatus() <= 299) || cnt == 0) {

            final EntityManager em = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();
                final WebhookAttempt webhookAttempt = new WebhookAttempt(response.getStatus(), NUMBER_OF_RETRY_WEBHOOK - cnt, webhookTrigger);
                em.persist(webhookAttempt);
                tx.commit();
            } catch (Exception e) {
                LOG.log(Level.WARNING,"Error adding a webhook trigger to the DB",e);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        } else {
            asyncRequest.retry(cnt);
        }
    }

    @Override
    public void failed(Throwable throwable) {
        cnt--;

        if (cnt == 0) {

            final EntityManager em = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();
                final WebhookAttempt webhookAttempt = new WebhookAttempt(-1, NUMBER_OF_RETRY_WEBHOOK - cnt, webhookTrigger);
                em.persist(webhookAttempt);
                tx.commit();
            } catch (Exception e) {
                LOG.log(Level.WARNING,"Error adding a webhook trigger to the DB",e);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        } else {
            asyncRequest.retry(cnt);
        }
    }
}
