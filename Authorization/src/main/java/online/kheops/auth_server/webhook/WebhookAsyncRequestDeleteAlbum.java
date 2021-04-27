package online.kheops.auth_server.webhook;

import online.kheops.auth_server.marshaller.WebhookWriter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.CompletionStageRxInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.*;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;
import static online.kheops.auth_server.util.Consts.SECONDE_BEFORE_RETRY_WEBHOOK;
import static online.kheops.auth_server.util.HttpHeaders.*;

public class WebhookAsyncRequestDeleteAlbum {

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ExecutorService CALLBACK_SCHEDULER = Executors.newFixedThreadPool(4);

    private static final Client CLIENT = ClientBuilder.newClient().register(WebhookWriter.class);

    private String url;
    private WebhookResult data;
    private String secret;

    public WebhookAsyncRequestDeleteAlbum(String url, WebhookResult data, String secret) {

        this.url = url;
        this.secret = secret;
        this.data = data;
    }

    public void firstRequest() {
        SCHEDULER.schedule(()->request(NUMBER_OF_RETRY_WEBHOOK),0, TimeUnit.SECONDS);
    }

    public void retry(int cnt) {
        SCHEDULER.schedule(()->request(cnt), SECONDE_BEFORE_RETRY_WEBHOOK * cnt, TimeUnit.SECONDS);
    }

    private void request(int cnt) {

        CompletionStageRxInvoker asyncInvoker = CLIENT.target(url).request()
                .header(X_KHEOPS_ATTEMPT, NUMBER_OF_RETRY_WEBHOOK - cnt + 1 + "/" + NUMBER_OF_RETRY_WEBHOOK)
                .header(X_KHEOPS_EVENT, data.getType().name().toLowerCase())
                .rx();

        if(secret != null) {
            final SignedEntity signedEntity = new SignedEntity(data, secret);
            final CompletionStage<Response> completionStage = asyncInvoker.post(Entity.entity(signedEntity, MediaType.APPLICATION_JSON));
            completionStage.thenAcceptAsync((response) -> {
                new WebhooksCallbacksDeleteAlbum(response,  cnt, this);
            }, CALLBACK_SCHEDULER);
            completionStage.exceptionally(throwable -> {
                new WebhooksCallbacksFailDeleteAlbum(throwable,  cnt, this);
                return null;
            });


        } else {
            final Entity entity = Entity.json(data);
            final CompletionStage<Response> completionStage = asyncInvoker.post(entity);
            completionStage.thenAcceptAsync(response -> new WebhooksCallbacksDeleteAlbum(response,  cnt, this), CALLBACK_SCHEDULER);
            completionStage.exceptionally(throwable -> {
                new WebhooksCallbacksFailDeleteAlbum(throwable,  cnt, this);
                return null;
            });
        }
    }

    public WebhookType getType() {
        return data.getType();
    }
}
