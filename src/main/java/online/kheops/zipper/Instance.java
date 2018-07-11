package online.kheops.zipper;


import java.util.Objects;

public final class Instance {
    final private String studyInstanceUID;
    final private String seriesInstanceUID;
    final private String SOPInstanceUID;

    private Instance(String studyInstanceUID, String seriesInstanceUID, String SOPInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID, "studyInstanceUID must not be null");
        this.seriesInstanceUID = Objects.requireNonNull(seriesInstanceUID, "seriesInstanceUID must not be null");
        this.SOPInstanceUID = Objects.requireNonNull(SOPInstanceUID, "SOPInstanceUID must not be null");
    }

    public static Instance newInstance(String studyInstanceUID, String seriesInstanceUID, String SOPInstanceUID) {
        return new Instance(studyInstanceUID, seriesInstanceUID, SOPInstanceUID);
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public String getSOPInstanceUID() {
        return SOPInstanceUID;
    }
}
