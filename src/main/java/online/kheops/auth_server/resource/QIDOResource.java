package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.generated.tables.Studies;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;


import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Context;
import javax.xml.ws.Provider;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import static online.kheops.auth_server.generated.tables.Users.*;

import static online.kheops.auth_server.generated.tables.Users.USERS;
import static online.kheops.auth_server.generated.tables.Capabilities.CAPABILITIES;
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

            long targetUserPk = User.findPkByUsername(username, em);
            if (callingUserPk != targetUserPk) {
                return Response.status(Response.Status.FORBIDDEN).entity("Access to study denied").build();
            }



           /* QUser user = QUser.user;
            QSeries series = QSeries.series;
            QStudy study = QStudy.study;*/

            String modality = "";
            if (uriInfo.getQueryParameters().containsKey("ModalitiesInStudy"))
                modality = uriInfo.getQueryParameters().get("ModalitiesInStudy").get(0);

            String patientID = "";
            if (uriInfo.getQueryParameters().containsKey("PatientID"))
                patientID = uriInfo.getQueryParameters().get("PatientID").get(0).replace("*", "");

            String patientName = "";
            if (uriInfo.getQueryParameters().containsKey("PatientName"))
                patientName = uriInfo.getQueryParameters().get("PatientName").get(0).replace("*", "");

            String studyDateBegin = "00000000";
            String studyDateEnd = "99999999";
            if (uriInfo.getQueryParameters().containsKey("StudyDate")) {
                studyDateBegin = uriInfo.getQueryParameters().get("StudyDate").get(0).split("-")[0];
                studyDateEnd = uriInfo.getQueryParameters().get("StudyDate").get(0).split("-")[1];
            }


            //JPAQuery<?> query = new JPAQuery<Void>(em);
            //List<?> uuss = query.from(series,user).select(user.pk, series.pk, series.seriesDescription, user.googleEmail)
            //        .join(user.series, series)
            //        .where()
            //        .where(series.modality.eq("CT"), user.pk.eq(callingUserPk))
            //        .fetch();
            //List<?> uuss = query.where(series.modality.eq("CT"))
            //        .fetch();


           /* JPAQueryFactory queryFactory = new JPAQueryFactory(em);
            List<Tuple> lst = queryFactory.selectFrom(user)
                    .select(study.studyInstanceUID, study.studyDate, study.studyTime, study.timezoneOffsetFromUTC, study.accessionNumber,
                            study.referringPhysicianName, study.patientName, study.patientID, study.patientBirthDate, study.patientSex,
                            study.studyID, series.pk, series.numberOfSeriesRelatedInstances,
                            //SQLExpressions.groupConcat(series.modality, "/"))
                            series.modality)
                    .innerJoin(user.series, series)
                    .where(user.pk.eq(callingUserPk))
                    .where(series.modality.startsWithIgnoreCase(modality))
                    .innerJoin(series.study, study)
                    .where(study.patientID.startsWithIgnoreCase(patientID))
                    .where(study.patientName.startsWithIgnoreCase(patientName))
                    .where(study.studyDate.between(studyDateBegin,studyDateEnd))
                    .where(study.populated.eq(true))
                    .where(series.populated.eq(true))
                    //.groupBy(study.studyInstanceUID)
                    //.orderBy(series.pk.asc())//.offset(10).limit(20)
                    //.transform(groupBy(study.studyInstanceUID).as(list(series.modality)))
                    .fetch();


            JPAQuery<?> query = new JPAQuery<Void>(em);
            Map<String, ?> result = query.from(series, user, study)

                    .where(user.pk.eq(callingUserPk))
                    .where(series.modality.startsWithIgnoreCase(modality))
                    .join(series.users, user)
                    .join(study.series, series)
                    .where(study.patientID.startsWithIgnoreCase(patientID))
                    .where(study.patientName.startsWithIgnoreCase(patientName))
                    .where(study.studyDate.between(studyDateBegin,studyDateEnd))
                    .where(study.populated.eq(true))
                    .where(series.populated.eq(true)).limit(4).offset(2)
                    .transform(GroupBy.groupBy(study.studyInstanceUID).as(study.patientID, GroupBy.set(series.modality),
                            GroupBy.sum(series.numberOfSeriesRelatedInstances), GroupBy.list(series)));*/





           // SQLQueryFactory(Configuration configuration, DataSource dataSource)
           // SQLQueryFactory(Configuration configuration, DataSource dataSource, boolean release)
           // SQLQueryFactory(Configuration configuration, javax.inject.Provider<Connection> connection)
           // SQLQueryFactory(SQLTemplates templates, javax.inject.Provider<Connection> connection)



            //Configuration conf = new Configuration(t);
            //SQLQuery<Tuple> q = new SQLQuery<Tuple>();
            //List<?> tt = q.from(user).select(user).fetch();

           /* SQLTemplates templates = new H2Templates();
            Configuration configuration = new Configuration(templates);
            String url = "jdbc:mysql://172.30.255.250/kheops";
            Connection conn = null;
            try {
                conn = DriverManager.getConnection(url, "kheopsuser", "mypwd");
            } catch (Exception e) {

            }
            try {
                Provider<Connection> provider = (Provider<Connection>) DriverManager.getConnection(url, "kheopsuser", "mypwd");
                SQLQueryFactory sqlQueryFactory = new SQLQueryFactory(SQLTemplates.DEFAULT, (javax.inject.Provider<Connection>) provider);
                //SQLQuery<Tuple> u2 = new SQLQuery<Tuple>();
                //Configuration conf = new Configuration(SQLTemplates.DEFAULT);

                List<User> test = sqlQueryFactory.from(user).select(user).fetch();
                LOG.info(test.toString());
            } catch (Exception e) {

            }*/

            /*JOOK*/

            String userName = "kheopsuser";
            String password = "mypwd";
            String url = "jdbc:mysql://172.30.255.250/kheops";

            // Connection is the only JDBC resource that we need
            // PreparedStatement and ResultSet are handled by jOOQ, internally
            try (Connection conn = DriverManager.getConnection(url, userName, password)) {

                DSLContext create = DSL.using(conn, SQLDialect.MYSQL);

                Result<Record14<String, String, String, String, String, String, String, String, String, String, String, Integer, BigDecimal, String>> result = create.select(STUDIES.STUDY_UID, STUDIES.STUDY_DATE, STUDIES.STUDY_TIME, STUDIES.TIMEZONE_OFFSET_FROM_UTC,
                STUDIES.ACCESSION_NUMBER, STUDIES.REFERRING_PHYSICIAN_NAME, STUDIES.PATIENT_NAME, STUDIES.PATIENT_ID, STUDIES.PATIENT_BIRTH_DATE, STUDIES.PATIENT_SEX,
                STUDIES.STUDY_ID, count(SERIES.PK), sum(SERIES.NUMBER_OF_SERIES_RELATED_INSTANCES), groupConcatDistinct(SERIES.MODALITY))
                        .from(USERS)
                        .join(USER_SERIES)
                        .on(USER_SERIES.USER_FK.eq(USERS.PK))
                        .join(SERIES)
                        .on(SERIES.PK.eq(USER_SERIES.SERIES_FK))
                        .join(STUDIES)
                        .on(STUDIES.PK.eq(SERIES.STUDY_FK))
                        .where(USERS.PK.eq(callingUserPk))
                        .groupBy(STUDIES.STUDY_UID)
                        .fetch();

                for (Record r : result) {

                    LOG.info(r.getValue(USERS.PK).toString());
                }
            }

            // For the sake of this tutorial, let's keep exception handling simple
            catch (Exception e) {
                e.printStackTrace();
            }
            /*JOOK*/

           // LOG.info(lst.toString());
            LOG.info(uriInfo.getQueryParameters().toString());


           /* attributesList = new ArrayList<>();

            for (Tuple results: lst) {
                Attributes attributes = new Attributes();

                Object[] res = results.toArray();
                int i =0;
                for (i=0; i<res.length; i++) {
                    if(Objects.isNull(res[i])) res[i]="NULL";
                    LOG.info(res[i].toString());
                }
                safeAttributeSetString(attributes, Tag.StudyInstanceUID, VR.UI, res[0].toString());
                safeAttributeSetString(attributes, Tag.StudyDate, VR.DA, res[1].toString());
                safeAttributeSetString(attributes, Tag.StudyTime, VR.TM, res[2].toString());
                safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, res[3].toString());
                safeAttributeSetString(attributes, Tag.AccessionNumber, VR.SH, res[4].toString());
                safeAttributeSetString(attributes, Tag.ReferringPhysicianName, VR.PN, res[5].toString());
                safeAttributeSetString(attributes, Tag.PatientName, VR.PN, res[6].toString());
                safeAttributeSetString(attributes, Tag.PatientID, VR.LO, res[7].toString());
                safeAttributeSetString(attributes, Tag.PatientBirthDate, VR.DA, res[8].toString());
                safeAttributeSetString(attributes, Tag.PatientSex, VR.CS, res[9].toString());
                safeAttributeSetString(attributes, Tag.StudyID, VR.SH, res[10].toString());
                attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, ((Long)res[11]).intValue());
                attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, ((Integer)res[12]));
                attributes.setString(Tag.ModalitiesInStudy, VR.CS, res[13].toString());

                safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

                attributesList.add(attributes);
            }*/


            attributesList = new ArrayList<>(Study.findAttributesByUserPK(callingUserPk, em));

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
