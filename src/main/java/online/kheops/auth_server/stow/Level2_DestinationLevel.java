package online.kheops.auth_server.stow;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;

import java.util.*;

public class Level2_DestinationLevel {
    //             Destination
    private Map<Album, Level3_SeriesLevel> level3;
    private boolean isSend;
    private Map<Series, Integer> allSeries;


    public Level2_DestinationLevel(Series series, boolean isNewSeries, Album destination, Integer numberOfInstances, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        level3 = new HashMap<>();
        level3.put(destination, new Level3_SeriesLevel(series, isNewSeries, numberOfInstances, isInbox, isNewInDestination));
        this.isSend = isSend;
        allSeries = new HashMap<>();
        allSeries.put(series, numberOfInstances);
    }

    public void addDestination(Series series, boolean isNewSeries, Album destination, Integer numberOfInstances, boolean isInbox, boolean isNewInDestination) {
        if (level3.containsKey(destination)) {
            level3.get(destination).addSeries(series, isNewSeries, numberOfInstances, isInbox, isNewInDestination);
        } else {
            level3.put(destination, new Level3_SeriesLevel(series, isNewSeries, numberOfInstances, isInbox, isNewInDestination));
        }
        allSeries.putIfAbsent(series, numberOfInstances);
    }

    public void setSend(boolean isSend) { this.isSend = isSend; }

    public Map<Album, Level3_SeriesLevel> getDestinations() { return level3; }
    public Level3_SeriesLevel getDestination(Album destination) { return level3.get(destination); }
    public boolean isSend() { return isSend; }

    public Map<Series, Integer> getAllSeries() { return allSeries; }

    @Override
    public String toString() {
        String s = "{";
        for(Map.Entry<Album, Level3_SeriesLevel> level3Entry:level3.entrySet()) {
            s += "\n\t\tdestination:" + level3Entry.getKey() + level3Entry.getValue().toString();
        }
        return s + '}';
    }
}