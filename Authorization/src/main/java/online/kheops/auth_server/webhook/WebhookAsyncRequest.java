package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.entity.WebhookTrigger;
import online.kheops.auth_server.marshaller.WebhookWriter;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.concurrent.*;
import java.util.logging.Logger;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;
import static online.kheops.auth_server.util.Consts.SECONDE_BEFORE_RETRY_WEBHOOK;
import static online.kheops.auth_server.util.HttpHeaders.*;

public class WebhookAsyncRequest {

    private static final Logger LOG = Logger.getLogger(WebhookAsyncRequest.class.getName());

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ExecutorService CALLBACK_SCHEDULER = Executors.newFixedThreadPool(4);

    private static final Client CLIENT = ClientBuilder.newClient().register(WebhookWriter.class);

    private Webhook webhook;
    private WebhookResult data;
    private WebhookTrigger webhookTrigger;

    public WebhookAsyncRequest(Webhook webhook, WebhookResult data, WebhookTrigger webhookTrigger) {

        this.webhookTrigger = webhookTrigger;
        this.webhook = webhook;
        this.data = data;
    }

    public void firstRequest() {
        SCHEDULER.schedule(()->request(NUMBER_OF_RETRY_WEBHOOK),0, TimeUnit.SECONDS);
    }

    public void retry(int cnt) {
        SCHEDULER.schedule(()->request(cnt),SECONDE_BEFORE_RETRY_WEBHOOK * cnt, TimeUnit.SECONDS);
    }

    private void request(int cnt) {

        CompletionStageRxInvoker asyncInvoker = CLIENT.target(webhook.getUrl()).request()
                .header(X_KHEOPS_DELIVERY, webhookTrigger.getId())
                .header(X_KHEOPS_ATTEMPT, NUMBER_OF_RETRY_WEBHOOK - cnt + 1 + "/" + NUMBER_OF_RETRY_WEBHOOK)
                .header(X_KHEOPS_EVENT, data.getType().name().toLowerCase())
                .rx();

        if(webhook.useSecret()) {
            final SignedEntity signedEntity = new SignedEntity(data, webhook.getSecret());
            final CompletionStage<Response> completionStage = asyncInvoker.post(Entity.entity(signedEntity, MediaType.APPLICATION_JSON));
            completionStage.thenAcceptAsync((response) -> {
                new WebhooksCallbacks(response, webhookTrigger, cnt, this);
            }, CALLBACK_SCHEDULER);
            completionStage.exceptionally(throwable -> {
                new WebhooksCallbacksFail(throwable, webhookTrigger, cnt, this);
                return null;
            });


        } else {
            final Entity entity = Entity.json(data);
            final CompletionStage<Response> completionStage = asyncInvoker.post(entity);
            completionStage.thenAcceptAsync((response) -> {
                new WebhooksCallbacks(response, webhookTrigger, cnt, this);
            }, CALLBACK_SCHEDULER);
            completionStage.exceptionally(throwable -> {
                new WebhooksCallbacksFail(throwable, webhookTrigger, cnt, this);
                return null;
            });
        }
    }

    public WebhookType getType() {
        return data.getType();
    }

    public String getRequestId() {
        return webhookTrigger.getId();
    }

    public Webhook getWebhook() { return webhook; }
}
