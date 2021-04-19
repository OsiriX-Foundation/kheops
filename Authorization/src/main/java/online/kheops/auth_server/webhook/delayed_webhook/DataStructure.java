package online.kheops.auth_server.webhook.delayed_webhook;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class DataStructure {

    private Map<StudySourceKey, DestinationSeries> studySourceKeyDestinationSeriesMap;

    DataStructure() { studySourceKeyDestinationSeriesMap = new HashMap<>(); }

    public void put (ScheduledFuture<?> scheduledFuture, Study study, Series series, Integer numberOfNewInstances, Source source, Album destination, boolean isInbox, boolean isNewInDestination, boolean isSend) {
        final StudySourceKey studySourceKey = new StudySourceKey(study, source);
        if (studySourceKeyDestinationSeriesMap.containsKey(studySourceKey)) {
            studySourceKeyDestinationSeriesMap.get(studySourceKey).cancelScheduledFuture();
            studySourceKeyDestinationSeriesMap.get(studySourceKey).add(scheduledFuture, series, numberOfNewInstances, destination, isInbox, isNewInDestination, isSend);
        } else {
            final DestinationSeries destinationSeries = new DestinationSeries(scheduledFuture, series, numberOfNewInstances, destination, isInbox, isNewInDestination, isSend);
            studySourceKeyDestinationSeriesMap.put(studySourceKey, destinationSeries);
        }
    }

    public DestinationSeries get(StudySourceKey studySourceKey) {
        return studySourceKeyDestinationSeriesMap.get(studySourceKey);
    }

    public void remove(Study study, Source source) {
        studySourceKeyDestinationSeriesMap.remove(new StudySourceKey(study, source));
    }
}
