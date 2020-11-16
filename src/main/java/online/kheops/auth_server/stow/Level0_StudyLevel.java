package online.kheops.auth_server.stow;

import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.webhook.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class Level0_StudyLevel {
    //              StudyUID
    private Map<Study, Level1_SourceLevel> studyHashMap;

    public Level0_StudyLevel() {
        studyHashMap = new HashMap<>();
    }

    public boolean containsStudy(Study study) {
        return studyHashMap.containsKey(study);
    }

    public Level1_SourceLevel get(Study study) {
        return studyHashMap.get(study);
    }

    public Level1_SourceLevel put(ScheduledFuture<?> scheduledFuture, Study study, Series series, boolean isNewStudy, boolean isNewSeries, Source source, Album destination, boolean isInbox, boolean isNewInDestination) {
        if (studyHashMap.containsKey(study)) {
            studyHashMap.get(study).addSeries(scheduledFuture, series, isNewSeries, source, destination, isInbox, isNewInDestination);
            return studyHashMap.get(study);
        } else {
            Level1_SourceLevel level1SourceLevel = new Level1_SourceLevel(scheduledFuture, isNewStudy);
            level1SourceLevel.addSeries(scheduledFuture, series, isNewSeries, source, destination, isInbox, isNewInDestination);
            return studyHashMap.put(study, level1SourceLevel);
        }
    }


    public String toString(Study study) {
        if(this.containsStudy(study)) {
            return "\nstudy:" + study + studyHashMap.get(study).toString();
        } else {
            return "empty";
        }
    }

    public Level1_SourceLevel remove(Study study) {
        return studyHashMap.remove(study);
    }
}