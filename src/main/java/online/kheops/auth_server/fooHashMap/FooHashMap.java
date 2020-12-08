package online.kheops.auth_server.fooHashMap;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;


public interface FooHashMap {

    void addHashMapData(Study study, Series series,
                        Album destination, boolean isInbox,
                        Integer numberOfNewInstances, Source source,
                        boolean isNewInDestination, boolean isSend);
}