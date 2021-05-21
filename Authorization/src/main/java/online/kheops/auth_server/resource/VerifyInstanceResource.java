package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UIDValidator;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.series.Series.getSeries;
import static online.kheops.auth_server.util.Consts.ALBUM;

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
            @FormParam(ALBUM) String albumId,

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
            @FormParam("bodyPartExamined") String bodyPartExamined,

            @FormParam("instancesUID") @UIDValidator String instancesUID) {

        final KheopsPrincipal kheopsPrincipal = (KheopsPrincipal) securityContext.getUserPrincipal();

        if (!kheopsPrincipal.hasSeriesAddAccess(studyInstanceUID, seriesInstanceUID)) {
            return Response.status(UNAUTHORIZED).build();
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

        final Study study;
        final Series series;

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {

            series = getSeries(studyInstanceUID, seriesInstanceUID, em);
            study = series.getStudy();

            if (!isSameSeries(series, seriesParam)) {
                return Response.status(UNAUTHORIZED).build();
            }

            if (!isSameStudy(study, studyParam)) {
                return Response.status(UNAUTHORIZED).build();
            }

        } catch (SeriesNotFoundException e) {
            kheopsPrincipal.getKheopsLogBuilder()
                    .action(KheopsLogBuilder.ActionType.VERIFY_INSTANCE)
                    .study(studyInstanceUID)
                    .series(seriesInstanceUID)
                    .log();
            return Response.status(NO_CONTENT).build();
        } finally {
            em.close();
        }

        kheopsPrincipal.getKheopsLogBuilder()
                .action(KheopsLogBuilder.ActionType.VERIFY_INSTANCE)
                .study(studyInstanceUID)
                .series(seriesInstanceUID)
                .log();
        return Response.status(NO_CONTENT).build();
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
        return (study.getStudyDate() == null ? studyParam.studyDate == null : study.getStudyDate().equals(studyParam.studyDate)) &&
                (study.getStudyTime() == null ? studyParam.studyTime == null : study.getStudyTime().equals(studyParam.studyTime)) &&
                (study.getStudyDescription() == null ? studyParam.studyDescription == null : study.getStudyDescription().equals(studyParam.studyDescription)) &&
                (study.getTimezoneOffsetFromUTC() == null ? studyParam.timzoneOffsetFromUtc == null : study.getTimezoneOffsetFromUTC().equals(studyParam.timzoneOffsetFromUtc)) &&
                (study.getAccessionNumber() == null ? studyParam.accessionNumber == null : study.getAccessionNumber().equals(studyParam.accessionNumber)) &&
                (study.getReferringPhysicianName() == null ? studyParam.referringPhysicianName == null : study.getReferringPhysicianName().equals(studyParam.referringPhysicianName)) &&
                (study.getPatientName() == null ? studyParam.patientName == null : study.getPatientName().equals(studyParam.patientName)) &&
                (study.getPatientID() == null ? studyParam.patientId == null : study.getPatientID().equals(studyParam.patientId)) &&
                (study.getPatientBirthDate() == null ? studyParam.patientBirthDate == null : study.getPatientBirthDate().equals(studyParam.patientBirthDate)) &&
                (study.getPatientSex() == null ? studyParam.patientSex == null : study.getPatientSex().equals(studyParam.patientSex)) &&
                (study.getStudyID() == null ? studyParam.studyId == null : study.getStudyID().equals(studyParam.studyId));
    }
}
