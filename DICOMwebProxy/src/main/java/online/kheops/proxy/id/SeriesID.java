package online.kheops.proxy.id;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.glassfish.jersey.media.multipart.MultiPart;

public final class SeriesID extends MultiPart {
    private final String studyUID;
    private final String seriesUID;

    private final String studyDate;
    private final String studyTime;
    private final String studyDescription;
    private final String timzoneOffsetFromUtc;
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
        this.timzoneOffsetFromUtc = timzoneOffsetFromUtc;
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
        this.timzoneOffsetFromUtc = null;
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
