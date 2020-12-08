package online.kheops.auth_server.fooHashMap;

import online.kheops.auth_server.entity.Series;

import java.util.HashMap;
import java.util.Set;

public class Level1Value {

    private boolean inbox;
    private HashMap<Series, Level2Value> seriesLevel2ValueHashMap;

    public Level1Value(Series series, Integer numberOfNewInstances, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        this.inbox = isInbox;
        seriesLevel2ValueHashMap = new HashMap<>();
        final Level2Value level2Value = new Level2Value(numberOfNewInstances, isNewInDestination, isSend);
        seriesLevel2ValueHashMap.put(series, level2Value);
    }

    public void add(Series series, Integer numberOfNewInstances, boolean isNewInDestination, boolean isSend) {
        if (seriesLevel2ValueHashMap.containsKey(series)) {
            seriesLevel2ValueHashMap.get(series).add(numberOfNewInstances, isSend);
        } else {
            final Level2Value level2Value = new Level2Value(numberOfNewInstances, isNewInDestination, isSend);
            seriesLevel2ValueHashMap.put(series, level2Value);
        }
    }


    public boolean isInbox() { return inbox; }
    public Set<Series> getSeries() { return seriesLevel2ValueHashMap.keySet(); }
    public Level2Value get(Series series) { return seriesLevel2ValueHashMap.get(series); }
}
