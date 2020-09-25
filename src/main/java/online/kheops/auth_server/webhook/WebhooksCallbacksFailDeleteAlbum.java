package online.kheops.auth_server.webhook;


import java.util.logging.Level;
import java.util.logging.Logger;

public class WebhooksCallbacksFailDeleteAlbum {
    private static final Logger LOG = Logger.getLogger(WebhooksCallbacksFailDeleteAlbum.class.getName());


    public WebhooksCallbacksFailDeleteAlbum(Throwable throwable, int cnt, WebhookAsyncRequestDeleteAlbum asyncRequest) {
        cnt--;
        LOG.log(Level.WARNING, "", throwable);

        if (cnt > 0) {
            asyncRequest.retry(cnt);
        }
    }
}
