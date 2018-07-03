package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.annotation.Secured;;
import online.kheops.auth_server.entity.User;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;


import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Context;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;


import static online.kheops.auth_server.generated.tables.Users.USERS;
import static online.kheops.auth_server.generated.tables.Series.SERIES;
import static online.kheops.auth_server.generated.tables.Studies.STUDIES;
import static online.kheops.auth_server.generated.tables.UserSeries.USER_SERIES;
import static org.jooq.impl.DSL.*;

import org.jooq.*;
import org.jooq.impl.*;


@Path("/users")
public class QIDOResource {

    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @GET
    @Secured
    @Path("/{user}/studies")
    @Produces("application/dicom+json")
    public Response getStudies(@PathParam("user") String username,
                               @Context SecurityContext securityContext) {

        // for now don't use any parameters

        // get a list of all the studies
        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        // is the user sharing a series, or requesting access to a new series

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        List<Attributes> attributesList;

        try {
            tx.begin();

            Integer offset = 0;
            Integer limit = 100;

            TableField ord = STUDIES.PATIENT_NAME;
            SortField orderBy = ord.asc();



            long targetUserPk = User.findPkByUsername(username, em);
            if (callingUserPk != targetUserPk) {
                return Response.status(Response.Status.FORBIDDEN).entity("Access to study denied").build();
            }

            String modality = "";
            if (uriInfo.getQueryParameters().containsKey("ModalitiesInStudy")) {
                modality = uriInfo.getQueryParameters().get("ModalitiesInStudy").get(0);
            }

            String patientID = "";
            if (uriInfo.getQueryParameters().containsKey("PatientID")) {
                patientID = uriInfo.getQueryParameters().get("PatientID").get(0).replace("*", "");
            }

            String patientName = "";
            if (uriInfo.getQueryParameters().containsKey("PatientName")) {
                patientName = uriInfo.getQueryParameters().get("PatientName").get(0).replace("*", "");
            }

            String studyDateBegin = "00000000";
            String studyDateEnd = "99999999";
            if (uriInfo.getQueryParameters().containsKey("StudyDate")) {
                studyDateBegin = uriInfo.getQueryParameters().get("StudyDate").get(0).split("-")[0];
                studyDateEnd = uriInfo.getQueryParameters().get("StudyDate").get(0).split("-")[1];
            }

            String accessionNumber = "";
            if (uriInfo.getQueryParameters().containsKey("AccessionNumber")) {
                accessionNumber = uriInfo.getQueryParameters().get("AccessionNumber").get(0).replace("*", "");
            }


            String userName = "kheopsuser";
            String password = "mypwd";
            String url = "jdbc:mysql://172.30.255.250/kheops";

            attributesList = new ArrayList<>();

            // Connection is the only JDBC resource that we need
            // PreparedStatement and ResultSet are handled by jOOQ, internally
            try (Connection conn = DriverManager.getConnection(url, userName, password)) {

                DSLContext create = DSL.using(conn, SQLDialect.MYSQL);

                Result<Record14<String, String, String, String, String, String, String, String, String, String, String, Integer, BigDecimal, String>> result = create
                        .select(isnull(STUDIES.STUDY_UID, "NULL").as(STUDIES.STUDY_UID.getName()),
                                isnull(STUDIES.STUDY_DATE, "NULL").as(STUDIES.STUDY_DATE.getName()),
                                isnull(STUDIES.STUDY_TIME, "NULL").as(STUDIES.STUDY_TIME.getName()),
                                isnull(STUDIES.TIMEZONE_OFFSET_FROM_UTC, "NULL").as(STUDIES.TIMEZONE_OFFSET_FROM_UTC.getName()),
                                isnull(STUDIES.ACCESSION_NUMBER, "NULL").as(STUDIES.ACCESSION_NUMBER.getName()),
                                isnull(STUDIES.REFERRING_PHYSICIAN_NAME, "NULL").as(STUDIES.REFERRING_PHYSICIAN_NAME.getName()),
                                isnull(STUDIES.PATIENT_NAME, "NULL").as(STUDIES.PATIENT_NAME.getName()),
                                isnull(STUDIES.PATIENT_ID, "NULL").as(STUDIES.PATIENT_ID.getName()),
                                isnull(STUDIES.PATIENT_BIRTH_DATE, "NULL").as(STUDIES.PATIENT_BIRTH_DATE.getName()),
                                isnull(STUDIES.PATIENT_SEX, "NULL").as(STUDIES.PATIENT_SEX.getName()),
                                isnull(STUDIES.STUDY_ID, "NULL").as(STUDIES.STUDY_ID.getName()),
                                isnull(count(SERIES.PK), 0).as("count:"+SERIES.PK.getName()),
                                sum(SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES).as("sum:"+SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES.getName()),
                                isnull(groupConcatDistinct(SERIES.MODALITY), "NULL").as("modalities"))
                        .from(USERS)
                        .join(USER_SERIES)
                            .on(USER_SERIES.USER_FK.eq(USERS.PK))
                        .join(SERIES)
                            .on(SERIES.PK.eq(USER_SERIES.SERIES_FK))
                        .join(STUDIES)
                            .on(STUDIES.PK.eq(SERIES.STUDY_FK))
                        .where(USERS.PK.eq(callingUserPk))
                            .and(SERIES.POPULATED.eq((byte)0x01))
                            .and(STUDIES.POPULATED.eq((byte)0x01))
                            .and(SERIES.MODALITY.lower().startsWith(modality.toLowerCase()))
                            .and(STUDIES.PATIENT_ID.lower().startsWith(patientID.toLowerCase()))
                            .and(STUDIES.PATIENT_NAME.lower().startsWith(patientName.toLowerCase()))
                            .and(STUDIES.STUDY_DATE.between(studyDateBegin, studyDateEnd))
                            .and(STUDIES.ACCESSION_NUMBER.lower().startsWith(accessionNumber.toLowerCase()))
                        .groupBy(STUDIES.STUDY_UID)
                        .orderBy(orderBy)
                        .offset(offset).limit(limit)
                        .fetch();


                for (Record r : result) {

                    //get all the modalities for the STUDY_UID
                    Result<Record1<String>> mod = create.select(groupConcatDistinct(SERIES.MODALITY))
                            .from(USERS)
                            .join(USER_SERIES)
                            .on(USER_SERIES.USER_FK.eq(USERS.PK))
                            .join(SERIES)
                            .on(SERIES.PK.eq(USER_SERIES.SERIES_FK))
                            .join(STUDIES)
                            .on(STUDIES.PK.eq(SERIES.STUDY_FK))
                            .where(USERS.PK.eq(callingUserPk))
                            .and(SERIES.POPULATED.eq((byte)0x01))
                            .and(STUDIES.POPULATED.eq((byte)0x01))
                            .and(STUDIES.STUDY_UID.eq(r.getValue(0).toString()))
                            .groupBy(STUDIES.STUDY_UID)
                            .fetch();

                    Attributes attributes = new Attributes();

                    safeAttributeSetString(attributes, Tag.StudyInstanceUID, VR.UI, r.getValue("study_uid").toString());
                    safeAttributeSetString(attributes, Tag.StudyDate, VR.DA, r.getValue("study_date").toString());
                    safeAttributeSetString(attributes, Tag.StudyTime, VR.TM, r.getValue("study_time").toString());
                    safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, r.getValue("timezone_offset_from_utc").toString());
                    safeAttributeSetString(attributes, Tag.AccessionNumber, VR.SH, r.getValue("accession_number").toString());
                    safeAttributeSetString(attributes, Tag.ReferringPhysicianName, VR.PN, r.getValue("referring_physician_name").toString());
                    safeAttributeSetString(attributes, Tag.PatientName, VR.PN, r.getValue("patient_name").toString());
                    safeAttributeSetString(attributes, Tag.PatientID, VR.LO, r.getValue("patient_id").toString());
                    safeAttributeSetString(attributes, Tag.PatientBirthDate, VR.DA, r.getValue("patient_birth_date").toString());
                    safeAttributeSetString(attributes, Tag.PatientSex, VR.CS, r.getValue("patient_sex").toString());
                    safeAttributeSetString(attributes, Tag.StudyID, VR.SH, r.getValue("study_id").toString());
                    attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, ((Integer)r.getValue("count:pk")));
                    attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, (((BigDecimal)r.getValue("sum:number_of_series_related_instances"))).intValue());
                    //attributes.setString(Tag.ModalitiesInStudy, VR.CS, r.getValue("modalities").toString());
                    attributes.setString(Tag.ModalitiesInStudy, VR.CS, mod.get(0).getValue(0).toString());

                    safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

                    attributesList.add(attributes);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            LOG.info("QueryParameters : "+uriInfo.getQueryParameters().toString());

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        GenericEntity<List<Attributes>> genericAttributesList = new GenericEntity<List<Attributes>>(attributesList) {};
        return Response.ok(genericAttributesList).build();
    }


    private static void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
        if (string != null) {
            attributes.setString(tag, vr, string);
        }
    }
}
