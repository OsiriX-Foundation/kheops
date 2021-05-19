package online.kheops.auth_server.study;

import online.kheops.auth_server.util.StudyQIDOParams;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import static online.kheops.auth_server.util.Consts.CUSTOM_DICOM_TAG_COMMENTS;
import static online.kheops.auth_server.util.Consts.CUSTOM_DICOM_TAG_FAVORITE;

public class StudyResponseDICOM {
    
    private String studyInstanceUID;
    private String studyID;
    private String studyDate;
    private String studyTime;
    private String timezoneOffsetFromUTC;
    private String studyDescription;
    private String referringPhysicianName;
    private String accessionNumber;
    private String patientName;
    private String patientBirthDate;
    private String patientID;
    private String patientSex;
    private int nbSeries;
    private int nbInstances;
    private String modalities;
    private int nbFavorite;
    private int nbComment;
    
    
    public StudyResponseDICOM(String studyInstanceUID, String studyID, String studyDate, String studyTime, String timezoneOffsetFromUTC, String studyDescription, 
                              String referringPhysicianName, String accessionNumber, String patientName, String patientBirthDate, String patientID, String patientSex, 
                              Long nbSeries, Long nbInstances, String modalities,Long nbFavorite, Long nbComment) {
        this.studyInstanceUID = studyInstanceUID;
        this.studyID = studyID;
        this.studyDate = studyDate;
        this.studyTime = studyTime;
        this.timezoneOffsetFromUTC = timezoneOffsetFromUTC;
        this.studyDescription = studyDescription;
        this.referringPhysicianName = referringPhysicianName;
        this.accessionNumber = accessionNumber;
        this.patientName = patientName;
        this.patientBirthDate = patientBirthDate;
        this.patientID = patientID;
        this.patientSex = patientSex;
        this.nbSeries = nbSeries.intValue();
        this.nbInstances = nbInstances.intValue();
        this.nbComment = nbComment.intValue();
        this.nbFavorite = nbFavorite.intValue();

        final SortedSet<String> mod = new TreeSet<>(Arrays.asList(modalities.substring(1, modalities.length() - 1).split(",")));
        mod.remove("NULL");
        this.modalities = mod.toString();
        this.modalities = this.modalities.substring(1, this.modalities.length() - 1);
    }

    public Attributes getAttribute(StudyQIDOParams qidoParams) {
        Attributes attributes = new Attributes();

        attributes.setValue(Tag.ModalitiesInStudy, VR.CS, modalities.split(","));

        //Tag Type (1) Required
        safeAttributeSetString(attributes, Tag.StudyInstanceUID, VR.UI, studyInstanceUID);

        //Tag Type (2) Required, Empty if Unknown
        safeAttributeSetString(attributes, Tag.StudyDate, VR.DA, studyDate);
        safeAttributeSetString(attributes, Tag.StudyTime, VR.TM, studyTime);
        safeAttributeSetString(attributes, Tag.AccessionNumber, VR.SH, accessionNumber);
        safeAttributeSetString(attributes, Tag.ReferringPhysicianName, VR.PN, referringPhysicianName);
        safeAttributeSetString(attributes, Tag.PatientName, VR.PN, patientName);
        safeAttributeSetString(attributes, Tag.PatientID, VR.LO, patientID);
        safeAttributeSetString(attributes, Tag.PatientBirthDate, VR.DA, patientBirthDate);
        safeAttributeSetString(attributes, Tag.PatientSex, VR.CS, patientSex);
        safeAttributeSetString(attributes, Tag.StudyID, VR.SH, studyID);
        attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, nbSeries);
        attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, nbInstances);

        //Tag Type (3) Optional
        if(qidoParams.includeStudyDescriptionField()) {
            safeAttributeSetString(attributes, Tag.StudyDescription, VR.CS, studyDescription);
        }
        safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, timezoneOffsetFromUTC);

        //Tag Custom
        if(qidoParams.includeFavoriteField()) {
            attributes.setInt(CUSTOM_DICOM_TAG_FAVORITE, VR.IS, nbFavorite);
        }
        if(qidoParams.includeCommentField()) {
            attributes.setInt(CUSTOM_DICOM_TAG_COMMENTS, VR.IS, nbComment);
        }

        //Others
        safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

        return attributes;
    }

    private void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
        if (string != null) {
            attributes.setString(tag, vr, string);
        }
    }
}
