package online.kheops.auth_server.webhook.delayed_webhook;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;


public interface DelayedWebhook {

    void addWebhookData(Study study, Series series,
                        Album destination, boolean isInbox,
                        Integer numberOfNewInstances, Source source,
                        boolean isNewInDestination, boolean isSend);
}