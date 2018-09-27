package online.kheops.proxy;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import java.util.Objects;

public final class InstanceID {
    private final SeriesID studyID;
    private final String sopInstanceUID;
    private final String sopClassUID;

    public InstanceID(SeriesID studyID, String sopInstanceUID, String sopClassUID) {
        this.studyID = Objects.requireNonNull(studyID);
        this.sopInstanceUID = Objects.requireNonNull(sopInstanceUID);
        this.sopClassUID = Objects.requireNonNull(sopClassUID);
    }

    public static InstanceID from(Attributes attributes) throws IllegalArgumentException {
        final String sopInstanceUID = attributes.getString(Tag.SOPInstanceUID);
        if (sopInstanceUID == null) {
            throw new IllegalArgumentException("Missing SOPInstanceUID");
        }
        final String sopClassUID = attributes.getString(Tag.SOPClassUID);
        if (sopClassUID == null) {
            throw new IllegalArgumentException("Missing SOPClassUID");
        }

        return new InstanceID(SeriesID.from(attributes), sopInstanceUID, sopClassUID);
    }

    public SeriesID getSeriesID() {
        return studyID;
    }

    public String getSOPInstanceUID() {
        return sopInstanceUID;
    }

    public String getSOPClassUID() {
        return sopClassUID;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof InstanceID &&
                studyID.equals(((InstanceID) o).getSeriesID()) &&
                sopInstanceUID.equals(((InstanceID) o).getSOPInstanceUID()) &&
                sopClassUID.equals(((InstanceID) o).getSOPClassUID());
    }

    @Override
    public int hashCode() {
        return studyID.hashCode() | sopInstanceUID.hashCode() | sopClassUID.hashCode();
    }

    @Override
    public String toString() {
        return studyID.toString() + " SOPInstanceUID:" + sopInstanceUID + " SOPClassUID:" + sopClassUID;
    }
}
