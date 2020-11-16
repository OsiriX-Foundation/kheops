package online.kheops.auth_server.stow;

import online.kheops.auth_server.entity.Series;

import java.util.*;

public class Level3_SeriesLevel {
    //              SeriesUID
    private Map<Series, Level4_InstancesLevel> series;
    private boolean isInbox;

    public Level3_SeriesLevel(Series series, boolean isNewSeries, boolean isInbox, boolean isNewInDestination) {
        this.series = new HashMap<>();
        addSeries(series, isNewSeries, isInbox, isNewInDestination);
    }


    public void addSeries(Series series, boolean isNewSeries, boolean isInbox, boolean isNewInDestination) {
        this.isInbox = isInbox;
        if (this.series != null) {
            if (!this.series.containsKey(series)) {
                this.series.put(series, new Level4_InstancesLevel(isNewSeries, isNewInDestination));
            }
        }
    }

    public Map<Series, Level4_InstancesLevel> getSeries() { return series; }

    public Level4_InstancesLevel getSeries(Series series) { return this.series.get(series); }

    public boolean isInbox() { return isInbox; }

    @Override
    public String toString() {
        String s = "\n\t\t\t{";
        for(Map.Entry<Series, Level4_InstancesLevel> stringSetEntry:series.entrySet()) {
            s += "{series:" +stringSetEntry.getKey() +
                    stringSetEntry.getValue()+ "}";
        }
        s += '}';
        return s;
    }
}