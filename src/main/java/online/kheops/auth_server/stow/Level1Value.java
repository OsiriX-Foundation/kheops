package online.kheops.auth_server.stow;

import online.kheops.auth_server.entity.Series;

import java.util.HashMap;
import java.util.Set;

public class Level1Value {

    private boolean newStudy;
    private boolean inbox;
    private HashMap<Series, Level2Value> seriesLevel2ValueHashMap;

    public Level1Value(Series series, boolean isNewStudy, boolean isNewSeries, Integer numberOfInstances, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        this.newStudy = isNewStudy;
        this.inbox = isInbox;
        seriesLevel2ValueHashMap = new HashMap<>();
        final Level2Value level2Value = new Level2Value(isNewSeries, numberOfInstances, isNewInDestination, isSend);
        seriesLevel2ValueHashMap.put(series, level2Value);
    }

    public void add(Series series, boolean isNewSeries, Integer numberOfInstances, boolean isNewInDestination, boolean isSend) {
        if (seriesLevel2ValueHashMap.containsKey(series)) {
            seriesLevel2ValueHashMap.get(series).add(isNewSeries, numberOfInstances, isNewInDestination, isSend);
        } else {
            final Level2Value level2Value = new Level2Value(isNewSeries, numberOfInstances, isNewInDestination, isSend);
            seriesLevel2ValueHashMap.put(series, level2Value);
        }
    }


    public boolean isNewStudy() { return newStudy; }
    public boolean isInbox() { return inbox; }
    public Set<Series> getSeries() { return seriesLevel2ValueHashMap.keySet(); }
    public Level2Value get(Series series) { return seriesLevel2ValueHashMap.get(series); }
}
