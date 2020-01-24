package online.kheops.auth_server.webhook;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.entity.WebhookHistory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;

public class WebhooksCallbacks implements InvocationCallback<Response> {
    private Webhook webhook;
    private boolean isManualTrigger;
    private int cnt;
    private WebhookAsyncRequest asyncRequest;

    public WebhooksCallbacks(Webhook webhook, boolean isManualTrigger, int cnt, WebhookAsyncRequest asyncRequest) {
        this.webhook = webhook;
        this.isManualTrigger = isManualTrigger;
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

                webhook = em.merge(webhook);

                final WebhookTypes webhookType;
                if (asyncRequest.getType() instanceof NewUserWebhook) {
                    webhookType = WebhookTypes.NEW_USER;
                } else if (asyncRequest.getType() instanceof NewSeriesWebhook) {
                    webhookType = WebhookTypes.NEW_SERIES;
                } else {
                    webhookType = WebhookTypes.NEW_SERIES;
                }

                final WebhookHistory webhookHistory = new WebhookHistory(asyncRequest.getRequestId(), NUMBER_OF_RETRY_WEBHOOK - cnt, response.getStatus(), isManualTrigger, webhookType, webhook);

                em.persist(webhookHistory);
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
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

                webhook = em.merge(webhook);

                final WebhookTypes webhookType;
                if (asyncRequest.getType() instanceof NewUserWebhook) {
                    webhookType = WebhookTypes.NEW_USER;
                } else if (asyncRequest.getType() instanceof NewSeriesWebhook) {
                    webhookType = WebhookTypes.NEW_SERIES;
                } else {
                    webhookType = WebhookTypes.NEW_SERIES;
                }

                final WebhookHistory webhookHistory = new WebhookHistory(asyncRequest.getRequestId(), NUMBER_OF_RETRY_WEBHOOK - cnt,-1, isManualTrigger, webhookType, webhook);

                em.persist(webhookHistory);
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
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
