package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.concurrent.Future;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;

public class WebhookAsyncRequest<T> {


    private static final Client CLIENT = ClientBuilder.newClient();

    private Webhook webhook;
    private boolean isManualTrigger;
    private T data;

    public WebhookAsyncRequest(Webhook webhook, T data, boolean isManualTrigger) {

        this.webhook = webhook;
        this.isManualTrigger = isManualTrigger;
        this.data = data;

        request(NUMBER_OF_RETRY_WEBHOOK);
    }

    public void retry(int cnt) {
        request(cnt);
    }

    private void request(int cnt) {
        Entity s = Entity.json(data);
        Future<Response> f = CLIENT.target(webhook.getUrl()).request().async()
                .post(Entity.json(data),
                        new WebhooksCallbacks<T>(webhook, isManualTrigger, cnt, this));
    }

    public T getType() {
        return data;
    }
}
