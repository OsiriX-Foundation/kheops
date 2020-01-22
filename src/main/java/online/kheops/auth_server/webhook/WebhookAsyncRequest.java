package online.kheops.auth_server.webhook;

import online.kheops.auth_server.entity.Webhook;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.security.Key;
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
        Entity<T> s = Entity.json(data);
        String a = s.getEntity().toString();
        try {
            String donnee = "This class represents application/x-www-form-urlencoded";
            Mac m = Mac.getInstance("HmacSHA1");
            Key k = new SecretKeySpec(webhook.getSecret().getBytes(), "HmacSHA1");
            m.init(k);
            byte[]b = m.doFinal(donnee.getBytes());
            String res = bytesToHex(b);
            res=res.split(":sdwdwdw").toString();
        } catch (Exception e) {

        }


        Future<Response> f = CLIENT.target(webhook.getUrl()).request().async()
                .post(Entity.json(data),
                        new WebhooksCallbacks<T>(webhook, isManualTrigger, cnt, this));
    }

    public T getType() {
        return data;
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
