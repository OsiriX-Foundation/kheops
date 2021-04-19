package online.kheops.auth_server.webhook;

import javax.ws.rs.core.Response;
import java.util.logging.Logger;

public class WebhooksCallbacksDeleteAlbum {
    private static final Logger LOG = Logger.getLogger(WebhooksCallbacksDeleteAlbum.class.getName());


    public WebhooksCallbacksDeleteAlbum(Response response, int cnt, WebhookAsyncRequestDeleteAlbum asyncRequest) {

        cnt--;

        if (!Response.Status.Family.familyOf(response.getStatus()).equals(Response.Status.Family.SUCCESSFUL) && cnt > 0) {
            asyncRequest.retry(cnt);
        }
    }
}
