package online.kheops.auth_server.fooHashMap;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class Level0 {

    private Map<Level0Key, Level0Value> level0KeyLevel0ValueMap;

    Level0() { level0KeyLevel0ValueMap = new HashMap<>(); }

    public void put (ScheduledFuture<?> scheduledFuture, Study study, Series series, Integer numberOfNewInstances, Source source, Album destination, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        final Level0Key level0Key = new Level0Key(study, source);
        if (level0KeyLevel0ValueMap.containsKey(level0Key)) {
            level0KeyLevel0ValueMap.get(level0Key).cancelScheduledFuture();
            level0KeyLevel0ValueMap.get(level0Key).add(scheduledFuture, series, numberOfNewInstances, destination, isInbox, isNewInDestination, isSend);
        } else {
            final Level0Value level0Value = new Level0Value(scheduledFuture, series, numberOfNewInstances, destination, isInbox, isNewInDestination, isSend);
            level0KeyLevel0ValueMap.put(level0Key, level0Value);
        }
    }

    public Level0Value get(Level0Key level0Key) {
        return level0KeyLevel0ValueMap.get(level0Key);
    }

    public void remove(Study study, Source source) {
        level0KeyLevel0ValueMap.remove(new Level0Key(study, source));
    }
}
