package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
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

        addWebhookAttemptToDb(cnt, response.getStatus());

        if (!Response.Status.Family.familyOf(response.getStatus()).equals(Response.Status.Family.SUCCESSFUL) && cnt > 0) {
            asyncRequest.retry(cnt);
        }
    }

    @Override
    public void failed(Throwable throwable) {
        cnt--;
        LOG.log(Level.WARNING, "FAIL WEBHOOK url :"+ this.asyncRequest.getWebhook().getUrl(), throwable);
        addWebhookAttemptToDb(cnt, -1);

        if (cnt > 0) {
            asyncRequest.retry(cnt);
        }
    }

    private void addWebhookAttemptToDb(int cnt, int status) {
        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            webhookTrigger = em.merge(webhookTrigger);
            //LOG.log(Level.WARNING,"webhook pk "+webhookTrigger.getWebhook().getPk());
            final WebhookAttempt webhookAttempt = new WebhookAttempt(status, NUMBER_OF_RETRY_WEBHOOK - cnt, webhookTrigger);
            em.persist(webhookAttempt);
            tx.commit();
        } catch (Exception e) {
            LOG.log(Level.WARNING,"Error adding a webhook attempt to the DB",e);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }
}
