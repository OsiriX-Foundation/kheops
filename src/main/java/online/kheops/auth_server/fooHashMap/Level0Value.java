package online.kheops.auth_server.fooHashMap;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public class Level0Value {

    private ScheduledFuture<?> scheduledFuture;
    private HashMap<Level1Key, Level1Value> level1KeyLevel1ValueHashMap;

    public Level0Value(ScheduledFuture<?> scheduledFuture, Series series, Integer numberOfNewInstances, Album destination, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        this.scheduledFuture = scheduledFuture;
        level1KeyLevel1ValueHashMap = new HashMap<>();
        final Level1Key level1Key = new Level1Key(destination);
        final Level1Value level1Value =  new Level1Value(series, numberOfNewInstances, isInbox, isNewInDestination, isSend);
        level1KeyLevel1ValueHashMap.put(level1Key, level1Value);
    }

    public void add(ScheduledFuture<?> scheduledFuture, Series series, Integer numberOfNewInstances, Album destination, boolean isInbox, boolean isNewInDestination, boolean isSend)  {
        this.scheduledFuture = scheduledFuture;
        final Level1Key level1Key = new Level1Key(destination);
        if (level1KeyLevel1ValueHashMap.containsKey(level1Key)) {
            level1KeyLevel1ValueHashMap.get(level1Key).add(series, numberOfNewInstances, isNewInDestination, isSend);
        } else {
            final Level1Value level1Value =  new Level1Value(series, numberOfNewInstances, isInbox, isNewInDestination, isSend);
            level1KeyLevel1ValueHashMap.put(level1Key, level1Value);
        }
    }

    public Set<Level1Key> getKeys() {
        return level1KeyLevel1ValueHashMap.keySet();
    }

    public Level1Value get(Level1Key level1Key) {
        return level1KeyLevel1ValueHashMap.get(level1Key);
    }

    public boolean cancelScheduledFuture() {
        return scheduledFuture.cancel(true);
    }

}
