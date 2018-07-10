package online.kheops.zipper;


public final class Instance {
    final private String studyInstanceUID;
    final private String seriesInstanceUID;
    final private String SOPInstanceUID;

    private Instance(String studyInstanceUID, String seriesInstanceUID, String SOPInstanceUID) {
        this.studyInstanceUID = studyInstanceUID;
        this.seriesInstanceUID = seriesInstanceUID;
        this.SOPInstanceUID = SOPInstanceUID;
    }

    public static Instance newInstance(String studyInstanceUID, String seriesInstanceUID, String SOPInstanceUID) {
        if (studyInstanceUID == null) {
            throw new IllegalArgumentException("studyInstanceUID can not be null");
        }
        if (seriesInstanceUID == null) {
            throw new IllegalArgumentException("seriesInstanceUID can not be null");
        }
        if (SOPInstanceUID == null) {
            throw new IllegalArgumentException("SOPInstanceUID can not be null");
        }
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
