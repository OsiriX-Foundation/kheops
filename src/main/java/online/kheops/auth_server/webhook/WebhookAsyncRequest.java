package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.security.Key;
import java.util.concurrent.Future;

import static online.kheops.auth_server.util.Consts.NUMBER_OF_RETRY_WEBHOOK;
import static online.kheops.auth_server.util.HttpHeaders.*;

public class WebhookAsyncRequest<T> {


    private static final Client CLIENT = ClientBuilder.newClient();

    private Webhook webhook;
    private boolean isManualTrigger;
    private T data;
    private String requestId;

    public WebhookAsyncRequest(Webhook webhook, T data, boolean isManualTrigger) {

        this.webhook = webhook;
        this.isManualTrigger = isManualTrigger;
        this.data = data;

        this.requestId = new WebhookRequestId().getRequestId();

        request(NUMBER_OF_RETRY_WEBHOOK);
    }

    public void retry(int cnt) {
        request(cnt);
    }

    private void request(int cnt) {
        final Entity<T> entity = Entity.json(data);

        //TODO recuperer le contenu de entity pour le hash avec le secret

        Invocation.Builder builder = CLIENT.target(webhook.getUrl()).request();
        if(webhook.useSecret()) {
            try {
                final Mac m = Mac.getInstance("HmacSHA1");
                final Key k = new SecretKeySpec(webhook.getSecret().getBytes(), "HmacSHA1");
                m.init(k);
                final byte[] b = m.doFinal(requestId.getBytes());
                final String hash = bytesToHex(b);
                builder.header(X_KHEOPS_SIGNATURE, hash);
            } catch (Exception e) {

            }
        }

        Future<Response> f = builder
                .header(X_KHEOPS_DELIVERY, requestId)
                .header(X_KHEOPS_ATTEMPT, NUMBER_OF_RETRY_WEBHOOK - cnt + 1 + "/" + NUMBER_OF_RETRY_WEBHOOK)
                .async()
                .post(entity, new WebhooksCallbacks(webhook, isManualTrigger, cnt, this));
    }

    public T getType() {
        return data;
    }

    public String getRequestId() {
        return requestId;
    }

    private static String bytesToHex(byte[] hash) {
        final StringBuilder hexString = new StringBuilder();
        for (byte hash1 : hash) {
            String hex = Integer.toHexString(0xff & hash1);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
