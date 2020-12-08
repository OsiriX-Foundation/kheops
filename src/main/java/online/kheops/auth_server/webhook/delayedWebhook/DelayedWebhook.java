package online.kheops.auth_server.webhook.delayedWebhook;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;


public interface DelayedWebhook {

    void addHashMapData(Study study, Series series,
                        Album destination, boolean isInbox,
                        Integer numberOfNewInstances, Source source,
                        boolean isNewInDestination, boolean isSend);
}