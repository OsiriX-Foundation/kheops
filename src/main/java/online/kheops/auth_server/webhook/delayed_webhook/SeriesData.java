package online.kheops.auth_server.webhook.delayed_webhook;

import online.kheops.auth_server.entity.Series;

import java.util.HashMap;
import java.util.Set;

public class SeriesData {

    private boolean inbox;
    private HashMap<Series, SeriesDetails> seriesSeriesDetailsHashMap;

    public SeriesData(Series series, Integer numberOfNewInstances, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        this.inbox = isInbox;
        seriesSeriesDetailsHashMap = new HashMap<>();
        final SeriesDetails seriesDetails = new SeriesDetails(numberOfNewInstances, isNewInDestination, isSend);
        seriesSeriesDetailsHashMap.put(series, seriesDetails);
    }

    public void add(Series series, Integer numberOfNewInstances, boolean isNewInDestination, boolean isSend) {
        if (seriesSeriesDetailsHashMap.containsKey(series)) {
            seriesSeriesDetailsHashMap.get(series).add(numberOfNewInstances, isSend);
        } else {
            final SeriesDetails seriesDetails = new SeriesDetails(numberOfNewInstances, isNewInDestination, isSend);
            seriesSeriesDetailsHashMap.put(series, seriesDetails);
        }
    }


    public boolean isInbox() { return inbox; }
    public Set<Series> getSeries() { return seriesSeriesDetailsHashMap.keySet(); }
    public SeriesDetails get(Series series) { return seriesSeriesDetailsHashMap.get(series); }
}
