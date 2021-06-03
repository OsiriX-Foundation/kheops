package online.kheops.proxy.id;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;

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
                (((SeriesID) o).modality == null ? modality == null : ((SeriesID) o).modality.equals(modality)) &&
                (((SeriesID) o).seriesDescription == null ? seriesDescription == null : ((SeriesID) o).seriesDescription.equals(seriesDescription)) &&
                ((SeriesID) o).seriesNumber == seriesNumber &&
                (((SeriesID) o).bodyPartExamined == null ? bodyPartExamined == null : ((SeriesID) o).bodyPartExamined.equals(bodyPartExamined)) &&
                (((SeriesID) o).timezoneOffsetFromUtc == null ? timezoneOffsetFromUtc == null : ((SeriesID) o).timezoneOffsetFromUtc.equals(timezoneOffsetFromUtc)) &&
                (((SeriesID) o).studyUID == null ? studyUID == null : ((SeriesID) o).studyUID.equals(studyUID)) &&
                (((SeriesID) o).studyDate == null ? studyDate == null : ((SeriesID) o).studyDate.equals(studyDate)) &&
                (((SeriesID) o).studyTime == null ? studyTime == null : ((SeriesID) o).studyTime.equals(studyTime)) &&
                (((SeriesID) o).studyDescription == null ? studyDescription == null : ((SeriesID) o).studyDescription.equals(studyDescription)) &&
                (((SeriesID) o).accessionNumber == null ? accessionNumber == null : ((SeriesID) o).accessionNumber.equals(accessionNumber)) &&
                (((SeriesID) o).referringPhysicianName == null ? referringPhysicianName == null : ((SeriesID) o).referringPhysicianName.equals(referringPhysicianName)) &&
                (((SeriesID) o).patientName == null ? patientName == null : ((SeriesID) o).patientName.equals(patientName)) &&
                (((SeriesID) o).patientId == null ? patientId == null : ((SeriesID) o).patientId.equals(patientId)) &&
                (((SeriesID) o).patientBirthDate == null ? patientBirthDate == null : ((SeriesID) o).patientBirthDate.equals(patientBirthDate)) &&
                (((SeriesID) o).patientSex == null ? patientSex == null : ((SeriesID) o).patientSex.equals(patientSex)) &&
                (((SeriesID) o).studyId == null ? studyId == null : ((SeriesID) o).studyId.equals(studyId));
    }


    @Override
    public int hashCode() {
        return studyUID.hashCode() | seriesUID.hashCode()
        | (studyDate == null ? 1 : studyDate.hashCode())
        | (studyTime == null ? 1 : studyTime.hashCode())
        | (studyDescription == null ? 1 : studyDescription.hashCode())
        | (timezoneOffsetFromUtc == null ? 1 : timezoneOffsetFromUtc.hashCode())
        | (accessionNumber == null ? 1 : accessionNumber.hashCode())
        | (referringPhysicianName == null ? 1 : referringPhysicianName.hashCode())
        | (patientName == null ? 1 : patientName.hashCode())
        | (patientId == null ? 1 : patientId.hashCode())
        | (patientBirthDate == null ? 1 : patientBirthDate.hashCode())
        | (patientSex == null ? 1 : patientSex.hashCode())
        | (studyId == null ? 1 : studyId.hashCode())
        | (modality == null ? 1 : modality.hashCode())
        | (seriesDescription == null ? 1 : seriesDescription.hashCode())
        | seriesNumber
        | (bodyPartExamined == null ? 1 : bodyPartExamined.hashCode());
    }

    @Override
    public String toString() {
        return "StudyInstanceUID:" + studyUID + " SeriesInstanceUID:" + seriesUID;
    }

}
