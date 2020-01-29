package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;
import online.kheops.auth_server.marshaller.WebhookWriter;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;
import static online.kheops.auth_server.util.Consts.SECONDE_BEFORE_RETRY_WEBHOOK;
import static online.kheops.auth_server.util.HttpHeaders.*;

public class WebhookAsyncRequest {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Client CLIENT = ClientBuilder.newClient().register(WebhookWriter.class);

    private Webhook webhook;
    private boolean isManualTrigger;
    private WebhookResult data;
    private String requestId;

    public WebhookAsyncRequest(Webhook webhook, WebhookResult data, boolean isManualTrigger) {

        this.webhook = webhook;
        this.isManualTrigger = isManualTrigger;
        this.data = data;

        this.requestId = new WebhookRequestId().getRequestId();

        request(NUMBER_OF_RETRY_WEBHOOK);
    }

    public void retry(int cnt) {
        scheduler.schedule(()->request(cnt),SECONDE_BEFORE_RETRY_WEBHOOK, TimeUnit.SECONDS);
    }

    private void request(int cnt) {

        AsyncInvoker asyncInvoker = CLIENT.target(webhook.getUrl()).request()
                .header(X_KHEOPS_DELIVERY, requestId)
                .header(X_KHEOPS_ATTEMPT, NUMBER_OF_RETRY_WEBHOOK - cnt + 1 + "/" + NUMBER_OF_RETRY_WEBHOOK)
                .header(X_KHEOPS_EVENT, data.getType().name().toLowerCase())
                .async();

        if(webhook.useSecret()) {
            final SignedEntity signedEntity = new SignedEntity(data, webhook.getSecret());
            asyncInvoker.post(Entity.entity(signedEntity, MediaType.APPLICATION_JSON), new WebhooksCallbacks(webhook, isManualTrigger, cnt, this));

        } else {
            final Entity entity = Entity.json(data);
            asyncInvoker.post(entity, new WebhooksCallbacks(webhook, isManualTrigger, cnt, this));
        }
    }

    public WebhookType getType() {
        return data.getType();
    }

    public String getRequestId() {
        return requestId;
    }

}
