package online.kheops.auth_server.webhook.delayed_webhook;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public class DestinationSeries {

    private ScheduledFuture<?> scheduledFuture;
    private HashMap<DestinationDetails, SeriesData> destinationDetailsSeriesDataHashMap;

    public DestinationSeries(ScheduledFuture<?> scheduledFuture, Series series, Integer numberOfNewInstances, Album destination, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        this.scheduledFuture = scheduledFuture;
        destinationDetailsSeriesDataHashMap = new HashMap<>();
        final DestinationDetails level1Key = new DestinationDetails(destination);
        final SeriesData seriesData =  new SeriesData(series, numberOfNewInstances, isInbox, isNewInDestination, isSend);
        destinationDetailsSeriesDataHashMap.put(level1Key, seriesData);
    }

    public void add(ScheduledFuture<?> scheduledFuture, Series series, Integer numberOfNewInstances, Album destination, boolean isInbox, boolean isNewInDestination, boolean isSend)  {
        this.scheduledFuture = scheduledFuture;
        final DestinationDetails level1Key = new DestinationDetails(destination);
        if (destinationDetailsSeriesDataHashMap.containsKey(level1Key)) {
            destinationDetailsSeriesDataHashMap.get(level1Key).add(series, numberOfNewInstances, isNewInDestination, isSend);
        } else {
            final SeriesData seriesData =  new SeriesData(series, numberOfNewInstances, isInbox, isNewInDestination, isSend);
            destinationDetailsSeriesDataHashMap.put(level1Key, seriesData);
        }
    }

    public Set<DestinationDetails> getKeys() {
        return destinationDetailsSeriesDataHashMap.keySet();
    }

    public SeriesData get(DestinationDetails destinationDetails) {
        return destinationDetailsSeriesDataHashMap.get(destinationDetails);
    }

    public boolean cancelScheduledFuture() {
        return scheduledFuture.cancel(true);
    }

}
