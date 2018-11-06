package online.kheops.zipper.instance;


import java.util.Objects;

public final class Instance {
    public static final String STUDY_INSTANCE_UID = "StudyInstanceUID";
    public static final String SERIES_INSTANCE_UID = "SeriesInstanceUID";
    public static final String SOP_INSTANCE_UID = "SopInstanceUID";

    private final String studyInstanceUID;
    private final String seriesInstanceUID;
    private final String sopInstanceUID;

    private Instance(String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID, STUDY_INSTANCE_UID);
        this.seriesInstanceUID = Objects.requireNonNull(seriesInstanceUID, SERIES_INSTANCE_UID);
        this.sopInstanceUID = Objects.requireNonNull(sopInstanceUID, SOP_INSTANCE_UID);
    }

    public static Instance newInstance(String studyInstanceUID, String seriesInstanceUID, String sopInstanceUID) {
        return new Instance(studyInstanceUID, seriesInstanceUID, sopInstanceUID);
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    String getSopInstanceUID() {
        return sopInstanceUID;
    }

    @Override
    public String toString() {
        return STUDY_INSTANCE_UID + ":" + studyInstanceUID + " " + SERIES_INSTANCE_UID + ":" + seriesInstanceUID + " " + SOP_INSTANCE_UID + ":" + sopInstanceUID;
    }
}
