package online.kheops.auth_server.study;

import online.kheops.auth_server.entity.Study;
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

    private Study study;
    private int nbSeries;
    private int nbInstances;
    private String modalities;
    private int nbFavorite;
    private int nbComment;

    public StudyResponseDICOM(Study study, Long nbSeries, Long nbInstances, String modalities,Long nbFavorite, Long nbComment) {
        this.study = study;
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
        safeAttributeSetString(attributes, Tag.StudyInstanceUID, VR.UI, study.getStudyInstanceUID());

        //Tag Type (2) Required, Empty if Unknown
        safeAttributeSetString(attributes, Tag.StudyDate, VR.DA, study.getStudyDate());
        safeAttributeSetString(attributes, Tag.StudyTime, VR.TM, study.getStudyTime());
        safeAttributeSetString(attributes, Tag.AccessionNumber, VR.SH, study.getAccessionNumber());
        safeAttributeSetString(attributes, Tag.ReferringPhysicianName, VR.PN, study.getReferringPhysicianName());
        safeAttributeSetString(attributes, Tag.PatientName, VR.PN, study.getPatientName());
        safeAttributeSetString(attributes, Tag.PatientID, VR.LO, study.getPatientID());
        safeAttributeSetString(attributes, Tag.PatientBirthDate, VR.DA, study.getPatientBirthDate());
        safeAttributeSetString(attributes, Tag.PatientSex, VR.CS, study.getPatientSex());
        safeAttributeSetString(attributes, Tag.StudyID, VR.SH, study.getPatientID());
        attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, nbSeries);
        attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, nbInstances);

        //Tag Type (3) Optional
        if(qidoParams.includeStudyDescriptionField()) {
            safeAttributeSetString(attributes, Tag.StudyDescription, VR.CS, study.getStudyDescription());
        }
        safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, study.getTimezoneOffsetFromUTC());

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

    @Override
    public String toString() {
        return "com:"+nbComment+" series:"+nbSeries+" instances:"+nbInstances+" fav:"+nbFavorite+" mod:"+modalities+" study:"+study.toString();
    }
}
