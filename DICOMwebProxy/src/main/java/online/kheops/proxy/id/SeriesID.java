package online.kheops.proxy.id;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import java.util.Objects;

public final class SeriesID extends MultiPart {
    private final String studyUID;
    private final String seriesUID;

    private final String studyDate;
    private final String studyTime;
    private final String studyDescription;
    private final String timezoneOffsetFromUtc;
    private final String accessionNumber;
    private final String referringPhysicianName;
    private final String patientName;
    private final String patientId;
    private final String patientBirthDate;
    private final String patientSex;
    private final String studyId;

    private final String modality;
    private final String seriesDescription;
    private final int seriesNumber;
    private final String bodyPartExamined;


    public SeriesID(String studyUID, String seriesUID, String studyDate, String studyTime, String studyDescription, String timzoneOffsetFromUtc, String accessionNumber, String referringPhysicianName, String patientName, String patientId, String patientBirthDate, String patientSex, String studyId, String modality, String seriesDescription, int seriesNumber, String bodyPartExamined) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;
        this.studyDate = studyDate;
        this.studyTime = studyTime;
        this.studyDescription = studyDescription;
        this.timezoneOffsetFromUtc = timzoneOffsetFromUtc;
        this.accessionNumber = accessionNumber;
        this.referringPhysicianName = referringPhysicianName;
        this.patientName = patientName;
        this.patientId = patientId;
        this.patientBirthDate = patientBirthDate;
        this.patientSex = patientSex;
        this.studyId = studyId;
        this.modality = modality;
        this.seriesDescription = seriesDescription;
        this.seriesNumber = seriesNumber;
        this.bodyPartExamined = bodyPartExamined;
    }

    public SeriesID(String studyUID, String seriesUID) {
        this.studyUID = studyUID;
        this.seriesUID = seriesUID;

        this.studyDate = null;
        this.studyTime = null;
        this.studyDescription = null;
        this.timezoneOffsetFromUtc = null;
        this.accessionNumber = null;
        this.referringPhysicianName = null;
        this.patientName = null;
        this.patientId = null;
        this.patientBirthDate = null;
        this.patientSex = null;
        this.studyId = null;
        this.modality = null;
        this.seriesDescription = null;
        this.seriesNumber = 0;
        this.bodyPartExamined = null;
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

        final String studyDate = attributes.getString(Tag.StudyDate);
        final String studyTime = attributes.getString(Tag.StudyTime);
        final String studyDescription = attributes.getString(Tag.StudyDescription);
        final String timzoneOffsetFromUtc = attributes.getString(Tag.TimezoneOffsetFromUTC);
        final String accessionNumber = attributes.getString(Tag.AccessionNumber);
        final String referringPhysicianName = attributes.getString(Tag.ReferringPhysicianName);
        final String patientName = attributes.getString(Tag.PatientName);
        final String patientId = attributes.getString(Tag.PatientID);
        final String patientBirthDate = attributes.getString(Tag.PatientBirthDate);
        final String patientSex = attributes.getString(Tag.PatientSex);
        final String studyId = attributes.getString(Tag.StudyID);

        final String modality = attributes.getString(Tag.Modality);
        final String seriesDescription = attributes.getString(Tag.SeriesDescription);
        final int seriesNumber = attributes.getInt(Tag.SeriesNumber, 0);
        final String bodyPartExamined = attributes.getString(Tag.BodyPartExamined);


        return new SeriesID(studyUID, seriesUID, studyDate, studyTime, studyDescription, timzoneOffsetFromUtc,
                accessionNumber, referringPhysicianName, patientName, patientId, patientBirthDate, patientSex,
                studyId, modality, seriesDescription, seriesNumber, bodyPartExamined);
    }

    public String getStudyUID() {
        return studyUID;
    }
    public String getSeriesUID() {
        return seriesUID;
    }

    public Entity getEntity() {
        final MultivaluedHashMap entity = new MultivaluedHashMap();
        entity.add("studyInstanceUID", studyUID);
        entity.add("studyDate", studyDate);
        entity.add("studyTime", studyTime);
        entity.add("timzoneOffsetFromUtc", timezoneOffsetFromUtc);
        entity.add("accessionNumber", accessionNumber);
        entity.add("referringPhysicianName", referringPhysicianName);
        entity.add("patientName", patientName);
        entity.add("patientId", patientId);
        entity.add("patientBirthDate", patientBirthDate);
        entity.add("patientSex", patientSex);
        entity.add("studyId", studyId);
        entity.add("studyDescription",studyDescription);
        entity.add("seriesInstanceUID", seriesUID);
        entity.add("modality", modality);
        entity.add("seriesDescription", seriesDescription);
        entity.add("seriesNumber", Integer.toString(seriesNumber));
        entity.add("bodyPartExamined", bodyPartExamined);

        return Entity.form(entity);
        }

        
    @Override
    public boolean equals(Object o) {
        return o instanceof SeriesID &&
                Objects.equals(((SeriesID) o).modality, modality) &&
                Objects.equals(((SeriesID) o).seriesDescription, seriesDescription) &&
                ((SeriesID) o).seriesNumber == seriesNumber &&
                Objects.equals(((SeriesID) o).bodyPartExamined, bodyPartExamined) &&
                Objects.equals(((SeriesID) o).timezoneOffsetFromUtc, timezoneOffsetFromUtc) &&
                Objects.equals(((SeriesID) o).studyUID, studyUID) &&
                Objects.equals(((SeriesID) o).studyDate, studyDate) &&
                Objects.equals(((SeriesID) o).studyTime, studyTime) &&
                Objects.equals(((SeriesID) o).studyDescription, studyDescription) &&
                Objects.equals(((SeriesID) o).accessionNumber, accessionNumber) &&
                Objects.equals(((SeriesID) o).referringPhysicianName, referringPhysicianName) &&
                Objects.equals(((SeriesID) o).patientName, patientName) &&
                Objects.equals(((SeriesID) o).patientId, patientId) &&
                Objects.equals(((SeriesID) o).patientBirthDate, patientBirthDate) &&
                Objects.equals(((SeriesID) o).patientSex, patientSex) &&
                Objects.equals(((SeriesID) o).studyId, studyId);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + studyUID.hashCode();
        result = prime * result + seriesUID.hashCode();
        result = prime * result + (studyDate == null ? 0 : studyDate.hashCode());
        result = prime * result + (studyTime == null ? 0 : studyTime.hashCode());
        result = prime * result + (studyDescription == null ? 0 : studyDescription.hashCode());
        result = prime * result + (timezoneOffsetFromUtc == null ? 0 : timezoneOffsetFromUtc.hashCode());
        result = prime * result + (accessionNumber == null ? 0 : accessionNumber.hashCode());
        result = prime * result + (referringPhysicianName == null ? 0 : referringPhysicianName.hashCode());
        result = prime * result + (patientName == null ? 0 : patientName.hashCode());
        result = prime * result + (patientId == null ? 0 : patientId.hashCode());
        result = prime * result + (patientBirthDate == null ? 0 : patientBirthDate.hashCode());
        result = prime * result + (patientSex == null ? 0 : patientSex.hashCode());
        result = prime * result + (studyId == null ? 0 : studyId.hashCode());
        result = prime * result + (modality == null ? 0 : modality.hashCode());
        result = prime * result + (seriesDescription == null ? 0 : seriesDescription.hashCode());
        result = prime * result + seriesNumber;
        result = prime * result + (bodyPartExamined == null ? 0 : bodyPartExamined.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "StudyInstanceUID:" + studyUID + " SeriesInstanceUID:" + seriesUID;
    }

}
