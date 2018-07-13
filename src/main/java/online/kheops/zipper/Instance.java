package online.kheops.zipper;


import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class Instance {
    final private String studyInstanceUID;
    final private String seriesInstanceUID;
    final private String SOPInstanceUID;

    private Instance(String studyInstanceUID, String seriesInstanceUID, String SOPInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID, "studyInstanceUID");
        this.seriesInstanceUID = Objects.requireNonNull(seriesInstanceUID, "seriesInstanceUID");
        this.SOPInstanceUID = Objects.requireNonNull(SOPInstanceUID, "SOPInstanceUID");
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

    @Override
    public String toString() {
        return "studyInstanceUID:" + studyInstanceUID + " seriesInstanceUID:" + seriesInstanceUID + " SOPInstanceUID:" + SOPInstanceUID;
    }
}
