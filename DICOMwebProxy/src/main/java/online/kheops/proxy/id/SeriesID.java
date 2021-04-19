package online.kheops.proxy.id;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.glassfish.jersey.media.multipart.MultiPart;

public final class SeriesID extends MultiPart {
    private final String studyUID;
    private final String seriesUID;

    public SeriesID(String studyUID, String seriesUID) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
    }

    public static SeriesID from(Attributes attributes) {
        final String studyUID = attributes.getString(Tag.StudyInstanceUID);
        if (studyUID == null) {
            throw new IllegalArgumentException("Missing StudyInstanceUID");
        }
        final String seriesUID = attributes.getString(Tag.SeriesInstanceUID);
        if (seriesUID == null) {
            throw new IllegalArgumentException("Missing SeriesInstanceUID");
        }

        return new SeriesID(studyUID, seriesUID);
    }

    public String getStudyUID() {
        return studyUID;
    }

    public String getSeriesUID() {
        return seriesUID;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SeriesID &&
                studyUID.equals(((SeriesID) o).getStudyUID()) &&
                seriesUID.equals(((SeriesID) o).getSeriesUID());
    }


    @Override
    public int hashCode() {
        return studyUID.hashCode() | seriesUID.hashCode();
    }

    @Override
    public String toString() {
        return "StudyInstanceUID:" + studyUID + " SeriesInstanceUID:" + seriesUID;
    }

}
