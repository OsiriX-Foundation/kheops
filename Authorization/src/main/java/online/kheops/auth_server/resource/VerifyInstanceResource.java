package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UIDValidator;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.VerifyResponse;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.study.Studies.getStudy;

@Path("/")
public class VerifyInstanceResource {

    @Context
    private SecurityContext securityContext;

    private static class StudyParam {
        String studyInstanceUID;
        String studyDate;
        String studyTime;
        String studyDescription;
        String timzoneOffsetFromUtc;
        String accessionNumber;
        String referringPhysicianName;
        String patientName;
        String patientId;
        String patientBirthDate;
        String patientSex;
        String studyId;
    }

    private static class SeriesParam {
        String seriesInstanceUID;
        String studyInstanceUID;
        String modality;
        String seriesDescription;
        int seriesNumber;
        String bodyPartExamined;
        String timzoneOffsetFromUtc;
    }

    @POST
    @Secured
    @Path("verifyInstance")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyInstance(

            @FormParam("studyInstanceUID") @UIDValidator String studyInstanceUID,
            @FormParam("studyDate") String studyDate,
            @FormParam("studyTime") String studyTime,
            @FormParam("studyDescription") String studyDescription,
            @FormParam("timzoneOffsetFromUtc") String timzoneOffsetFromUtc,
            @FormParam("accessionNumber") String accessionNumber,
            @FormParam("referringPhysicianName") String referringPhysicianName,
            @FormParam("patientName") String patientName,
            @FormParam("patientId") String patientId,
            @FormParam("patientBirthDate") String patientBirthDate,
            @FormParam("patientSex") String patientSex,
            @FormParam("studyId") String studyId,

            @FormParam("seriesInstanceUID") @UIDValidator String seriesInstanceUID,
            @FormParam("modality") String modality,
            @FormParam("seriesDescription") String seriesDescription,
            @FormParam("seriesNumber") int seriesNumber,
            @FormParam("bodyPartExamined") String bodyPartExamined) {

        final KheopsPrincipal kheopsPrincipal = (KheopsPrincipal) securityContext.getUserPrincipal();

        KheopsLogBuilder kheopsLogBuilder = kheopsPrincipal.getKheopsLogBuilder()
                .action(KheopsLogBuilder.ActionType.VERIFY_INSTANCE)
                .study(studyInstanceUID)
                .series(seriesInstanceUID);

        if (!kheopsPrincipal.hasSeriesAddAccess(studyInstanceUID, seriesInstanceUID)) {
            kheopsLogBuilder.reason("unauthorized to upload this series")
                    .log();
            return Response.status(OK).entity(new VerifyResponse(false, false, null, null)).build();
        }

        final StudyParam studyParam = new StudyParam();
        studyParam.studyInstanceUID = studyInstanceUID;
        studyParam.studyDate = studyDate;
        studyParam.studyTime = studyTime;
        studyParam.studyDescription = studyDescription;
        studyParam.timzoneOffsetFromUtc = timzoneOffsetFromUtc;
        studyParam.accessionNumber = accessionNumber;
        studyParam.referringPhysicianName = referringPhysicianName;
        studyParam.patientName = patientName;
        studyParam.patientId = patientId;
        studyParam.patientBirthDate = patientBirthDate;
        studyParam.patientSex = patientSex;
        studyParam.studyId = studyId;

        final SeriesParam seriesParam = new SeriesParam();
        seriesParam.bodyPartExamined = bodyPartExamined;
        seriesParam.modality = modality;
        seriesParam.seriesDescription = seriesDescription;
        seriesParam.seriesInstanceUID = seriesInstanceUID;
        seriesParam.timzoneOffsetFromUtc = timzoneOffsetFromUtc;
        seriesParam.studyInstanceUID = studyInstanceUID;
        seriesParam.seriesNumber = seriesNumber;

        Study study = null;
        Series series = null;

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();

        try {

            study = getStudy(studyInstanceUID, em);

            if (!isSameStudy(study, studyParam)) {
                final List<String> unmatchingTags = getReason(study, studyParam);
                kheopsLogBuilder.isValid(false)
                        .reason("some tags are different : " + String.join(",", unmatchingTags))
                        .log();
                return Response.status(OK).entity(new VerifyResponse(false, true, "some tags are different : see 'unmatching_tags'", unmatchingTags)).build();
            }

            series = getSeries(studyInstanceUID, seriesInstanceUID, em);

            if (!isSameSeries(series, seriesParam)) {
                final List<String> unmatchingTags = getReason(series, seriesParam);
                kheopsLogBuilder.isValid(false)
                        .reason("some tags are different : " + String.join(",", unmatchingTags))
                        .log();
                return Response.status(OK).entity(new VerifyResponse(false, true, "some tags are different : see 'unmatching_tags'", unmatchingTags)).build();
            }

        } catch (StudyNotFoundException e) {
            tx.begin();
            
            study = new Study(studyInstanceUID);
            final Attributes studyAttributes = new Attributes();

            studyAttributes.setString(Tag.StudyDate, VR.DA, studyDate);
            studyAttributes.setString(Tag.StudyTime, VR.TM, studyTime);
            studyAttributes.setString(Tag.StudyDescription, VR.LO, studyDescription);
            studyAttributes.setString(Tag.TimezoneOffsetFromUTC, VR.SH, timzoneOffsetFromUtc);
            studyAttributes.setString(Tag.AccessionNumber, VR.SH, accessionNumber);
            referringPhysicianName = referringPhysicianName != null && referringPhysicianName.equals("^^^^") ? null : referringPhysicianName;
            studyAttributes.setString(Tag.ReferringPhysicianName, VR.PN, referringPhysicianName);
            studyAttributes.setString(Tag.PatientName, VR.PN, patientName);
            studyAttributes.setString(Tag.PatientID, VR.LO, patientId);
            studyAttributes.setString(Tag.PatientBirthDate, VR.DA, patientBirthDate);
            studyAttributes.setString(Tag.PatientSex, VR.CS, patientSex);
            studyAttributes.setString(Tag.StudyID, VR.SH, studyId);

            study.mergeAttributes(studyAttributes);
            study.setPopulated(false);
            em.persist(study);

            
            series = new Series(seriesInstanceUID);
            final Attributes seriesAttributes = new Attributes();
            seriesAttributes.setString(Tag.Modality, VR.CS, modality);
            seriesAttributes.setString(Tag.BodyPartExamined, VR.CS, bodyPartExamined);
            seriesAttributes.setString(Tag.SeriesDescription, VR.LO, seriesDescription);
            seriesAttributes.setString(Tag.TimezoneOffsetFromUTC, VR.SH, timzoneOffsetFromUtc);
            seriesAttributes.setInt(Tag.SeriesNumber, VR.IS, seriesNumber);

            series.mergeAttributes(seriesAttributes);

            series.setStudy(study);
            series.setPopulated(false);

            em.persist(series);

            tx.commit();
        } catch (SeriesNotFoundException e) {
            tx.begin();

            series = new Series(seriesInstanceUID);
            final Attributes attributes = new Attributes();
            attributes.setString(Tag.Modality, VR.CS, modality);
            attributes.setString(Tag.BodyPartExamined, VR.CS, bodyPartExamined);
            attributes.setString(Tag.SeriesDescription, VR.LO, seriesDescription);
            attributes.setString(Tag.TimezoneOffsetFromUTC, VR.SH, timzoneOffsetFromUtc);
            attributes.setInt(Tag.SeriesNumber, VR.IS, seriesNumber);

            series.mergeAttributes(attributes);

            series.setStudy(study);
            series.setPopulated(false);

            em.persist(series);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        kheopsLogBuilder.isValid(true)
                .log();
        return Response.status(OK).entity(new VerifyResponse(true, true, null, null)).build();
    }

    private static boolean isSameSeries(Series series, SeriesParam seriesParam) {
        return (series.getModality() == null ? seriesParam.modality == null : series.getModality().equals(seriesParam.modality)) &&
                (series.getSeriesDescription() == null ? seriesParam.seriesDescription == null : series.getSeriesDescription().equals(seriesParam.seriesDescription)) &&
                series.getSeriesNumber() == seriesParam.seriesNumber &&
                (series.getBodyPartExamined() == null ? seriesParam.bodyPartExamined == null : series.getBodyPartExamined().equals(seriesParam.bodyPartExamined)) &&
                (series.getTimezoneOffsetFromUTC() == null ? seriesParam.timzoneOffsetFromUtc == null : series.getTimezoneOffsetFromUTC().equals(seriesParam.timzoneOffsetFromUtc)) &&
                (series.getStudy().getStudyInstanceUID() == null ? seriesParam.studyInstanceUID == null : series.getStudy().getStudyInstanceUID().equals(seriesParam.studyInstanceUID));
    }

    private static boolean isSameStudy(Study study, StudyParam studyParam) {
        return (study.getStudyDate() == null ? studyParam.studyDate == null : studyParam.studyDate == null || (study.getStudyDate().equals(studyParam.studyDate))) &&
                (study.getStudyTime() == null ? studyParam.studyTime == null : studyParam.studyTime == null || (study.getStudyTime().equals(studyParam.studyTime))) &&
                (study.getStudyDescription() == null ? studyParam.studyDescription == null : studyParam.studyDescription == null || (study.getStudyDescription().equals(studyParam.studyDescription))) &&
                (study.getTimezoneOffsetFromUTC() == null ? studyParam.timzoneOffsetFromUtc == null : studyParam.timzoneOffsetFromUtc == null || (study.getTimezoneOffsetFromUTC().equals(studyParam.timzoneOffsetFromUtc))) &&
                (study.getAccessionNumber() == null ? studyParam.accessionNumber == null : studyParam.accessionNumber == null || (study.getAccessionNumber().equals(studyParam.accessionNumber))) &&
                isSamePN(study.getReferringPhysicianName(), studyParam.referringPhysicianName) &&
                isSamePN(study.getPatientName(), studyParam.patientName) &&
                (study.getPatientID() == null ? studyParam.patientId == null : studyParam.patientId == null || (study.getPatientID().equals(studyParam.patientId))) &&
                (study.getPatientBirthDate() == null ? studyParam.patientBirthDate == null : studyParam.patientBirthDate == null || (study.getPatientBirthDate().equals(studyParam.patientBirthDate))) &&
                (study.getPatientSex() == null ? studyParam.patientSex == null : studyParam.patientSex == null || (study.getPatientSex().equals(studyParam.patientSex))) &&
                (study.getStudyID() == null ? studyParam.studyId == null : study.getStudyID().equals(studyParam.studyId));
    }

    private static List<String> getReason(Series series, SeriesParam seriesParam) {
        final List unmatchingTags = new ArrayList<String>();
        if (!(series.getModality() == null ? seriesParam.modality == null : series.getModality().equals(seriesParam.modality)))
            unmatchingTags.add(String.valueOf(Tag.Modality));
        if (!(series.getSeriesDescription() == null ? seriesParam.seriesDescription == null : series.getSeriesDescription().equals(seriesParam.seriesDescription)))
            unmatchingTags.add(String.valueOf(Tag.SeriesDescription));
        if (!(series.getSeriesNumber() == seriesParam.seriesNumber))
            unmatchingTags.add(String.valueOf(Tag.SeriesNumber));
        if (!(series.getBodyPartExamined() == null ? seriesParam.bodyPartExamined == null : series.getBodyPartExamined().equals(seriesParam.bodyPartExamined)))
            unmatchingTags.add(String.valueOf(Tag.BodyPartExamined));
        if (!(series.getTimezoneOffsetFromUTC() == null ? seriesParam.timzoneOffsetFromUtc == null : series.getTimezoneOffsetFromUTC().equals(seriesParam.timzoneOffsetFromUtc)))
            unmatchingTags.add(String.valueOf(Tag.TimezoneOffsetFromUTC));
        if (!(series.getStudy().getStudyInstanceUID() == null ? seriesParam.studyInstanceUID == null : series.getStudy().getStudyInstanceUID().equals(seriesParam.studyInstanceUID)))
            unmatchingTags.add(String.valueOf(Tag.StudyInstanceUID));

        return unmatchingTags;
    }

    private static List<String> getReason(Study study, StudyParam studyParam) {
        final List unmatchingTags = new ArrayList<String>();
        if (!(study.getStudyDate() == null ? studyParam.studyDate == null : studyParam.studyDate == null || (study.getStudyDate().equals(studyParam.studyDate))))
            unmatchingTags.add(String.valueOf(Tag.StudyDate));
        if (!(study.getStudyTime() == null ? studyParam.studyTime == null : studyParam.studyTime == null || (study.getStudyTime().equals(studyParam.studyTime))))
            unmatchingTags.add(String.valueOf(Tag.StudyTime));
        if (!(study.getStudyDescription() == null ? studyParam.studyDescription == null : studyParam.studyDescription == null || (study.getStudyDescription().equals(studyParam.studyDescription))))
            unmatchingTags.add(String.valueOf(Tag.StudyDescription));
        if (!(study.getTimezoneOffsetFromUTC() == null ? studyParam.timzoneOffsetFromUtc == null : studyParam.timzoneOffsetFromUtc == null || (study.getTimezoneOffsetFromUTC().equals(studyParam.timzoneOffsetFromUtc))))
            unmatchingTags.add(String.valueOf(Tag.TimezoneOffsetFromUTC));
        if (!(study.getAccessionNumber() == null ? studyParam.accessionNumber == null : studyParam.accessionNumber == null || (study.getAccessionNumber().equals(studyParam.accessionNumber))))
            unmatchingTags.add(String.valueOf(Tag.AccessionNumber));
        if (!(isSamePN(study.getReferringPhysicianName(), studyParam.referringPhysicianName)))
            unmatchingTags.add(String.valueOf(Tag.ReferringPhysicianName));
        if (!(isSamePN(study.getPatientName(), studyParam.patientName)))
            unmatchingTags.add(String.valueOf(Tag.PatientName));
        if (!(study.getPatientID() == null ? studyParam.patientId == null : studyParam.patientId == null || (study.getPatientID().equals(studyParam.patientId))))
            unmatchingTags.add(String.valueOf(Tag.PatientID));
        if (!(study.getPatientBirthDate() == null ? studyParam.patientBirthDate == null : studyParam.patientBirthDate == null || (study.getPatientBirthDate().equals(studyParam.patientBirthDate))))
            unmatchingTags.add(String.valueOf(Tag.PatientBirthDate));
        if (!(study.getPatientSex() == null ? studyParam.patientSex == null : studyParam.patientSex == null || (study.getPatientSex().equals(studyParam.patientSex))))
            unmatchingTags.add(String.valueOf(Tag.PatientSex));
        if (!(study.getStudyID() == null ? studyParam.studyId == null : study.getStudyID().equals(studyParam.studyId)))
            unmatchingTags.add(String.valueOf(Tag.StudyID));

        return unmatchingTags;
    }

    private static boolean isSamePN(final String personNameA, final String personNameB) {
        String pna = personNameA == null ? "" : personNameA.replace("^", "").replace("=", "");
        String pnb = personNameB == null ? "" : personNameB.replace("^", "").replace("=", "");
        return pna.equals("") ? pnb.equals("") : pnb.equals("") || pna.equals(pnb);
    }
}
