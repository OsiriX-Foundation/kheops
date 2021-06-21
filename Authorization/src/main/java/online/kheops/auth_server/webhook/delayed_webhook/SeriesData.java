package online.kheops.auth_server.webhook.delayed_webhook;

import online.kheops.auth_server.entity.Series;

import java.util.HashMap;
import java.util.Set;

public class SeriesData {

    private boolean inbox;
    private HashMap<Long, SeriesDetails> seriesPkSeriesDetailsHashMap;

    public SeriesData(Series series, Integer numberOfNewInstances, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        this.inbox = isInbox;
        seriesPkSeriesDetailsHashMap = new HashMap<>();
        final SeriesDetails seriesDetails = new SeriesDetails(numberOfNewInstances, isNewInDestination, isSend);
        seriesPkSeriesDetailsHashMap.put(series.getPk(), seriesDetails);
    }

    public void add(Series series, Integer numberOfNewInstances, boolean isNewInDestination, boolean isSend) {
        if (seriesPkSeriesDetailsHashMap.containsKey(series.getPk())) {
            seriesPkSeriesDetailsHashMap.get(series.getPk()).add(numberOfNewInstances, isSend);
        } else {
            final SeriesDetails seriesDetails = new SeriesDetails(numberOfNewInstances, isNewInDestination, isSend);
            seriesPkSeriesDetailsHashMap.put(series.getPk(), seriesDetails);
        }
    }


    public boolean isInbox() { return inbox; }
    public Set<Long> getSeriesPk() { return seriesPkSeriesDetailsHashMap.keySet(); }
    public SeriesDetails get(Series series) { return seriesPkSeriesDetailsHashMap.get(series.getPk()); }
}
