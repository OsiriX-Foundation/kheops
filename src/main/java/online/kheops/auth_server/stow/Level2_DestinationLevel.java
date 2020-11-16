package online.kheops.auth_server.stow;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;

import java.util.*;

public class Level2_DestinationLevel {
    //             Destination
    private Map<Album, Level3_SeriesLevel> level3;

    public Level2_DestinationLevel(Series series, boolean isNewSeries, Album destination, boolean isInbox, boolean isNewInDestination) {
        level3 = new HashMap<>();
        level3.put(destination, new Level3_SeriesLevel(series, isNewSeries, isInbox, isNewInDestination));
    }

    public void addDestination(Series series, boolean isNewSeries, Album destination, boolean isInbox, boolean isNewInDestination) {
        if (level3.containsKey(destination)) {
            level3.get(destination).addSeries(series, isNewSeries, isInbox, isNewInDestination);
        } else {
            level3.put(destination, new Level3_SeriesLevel(series, isNewSeries, isInbox, isNewInDestination));
        }
    }


    public Map<Album, Level3_SeriesLevel> getDestinations() { return level3; }
    public Level3_SeriesLevel getDestination(Album destination) { return level3.get(destination); }

    @Override
    public String toString() {
        String s = "{";
        for(Map.Entry<Album, Level3_SeriesLevel> level3Entry:level3.entrySet()) {
            s += "\n\t\tdestination:" + level3Entry.getKey() + level3Entry.getValue().toString();
        }
        return s + '}';
    }
}