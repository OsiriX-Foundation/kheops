package online.kheops.auth_server.resource;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.Tuple;
import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.entity.*;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

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



            QUser u = QUser.user;
            QSeries s = QSeries.series;
            QStudy st = QStudy.study;

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
            String studyDateEnd = "999999999";
            if (uriInfo.getQueryParameters().containsKey("StudyDate")) {
                studyDateBegin = uriInfo.getQueryParameters().get("StudyDate").get(0).split("-")[0];
                studyDateEnd = uriInfo.getQueryParameters().get("StudyDate").get(0).split("-")[1];
            }


            //JPAQuery<?> query = new JPAQuery<Void>(em);
            //List<?> uuss = query.from(s,u).select(u.pk, s.pk, s.seriesDescription, u.googleEmail)
            //        .join(u.series, s)
            //        .where()
            //        .where(s.modality.eq("CT"), u.pk.eq(callingUserPk))
            //        .fetch();
            //List<?> uuss = query.where(s.modality.eq("CT"))
            //        .fetch();

            JPAQueryFactory queryFactory = new JPAQueryFactory(em);
            List<Tuple> lst = queryFactory.selectFrom(u)
                    .select(st.studyInstanceUID, st.studyDate, st.studyTime, st.timezoneOffsetFromUTC, st.accessionNumber, st.referringPhysicianName, st.patientName, st.patientID, st.patientBirthDate, st.patientSex, st.studyID, s.numberOfSeriesRelatedInstances, s.numberOfSeriesRelatedInstances, s.modality)
                    .innerJoin(u.series, s)
                    .where(u.pk.eq(callingUserPk))
                    .where(s.modality.startsWithIgnoreCase(modality))
                    .innerJoin(s.study, st)
                    .where(st.patientID.startsWithIgnoreCase(patientID))
                    .where(st.patientName.startsWithIgnoreCase(patientName))
                    .where(st.studyDate.between(studyDateBegin,studyDateEnd))
                    .where(st.populated.eq(true))
                    .where(s.populated.eq(true))
                    .groupBy(st)
                    .fetch();


            LOG.info(lst.toString());
            LOG.info(uriInfo.getQueryParameters().toString());


            attributesList = new ArrayList<>();

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
                attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, ((Integer)res[11]));
                attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, ((Integer)res[12]));
                attributes.setString(Tag.ModalitiesInStudy, VR.CS, res[13].toString());

                safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");

                attributesList.add(attributes);
            }



            //attributesList = new ArrayList<>(Study.findAttributesByUserPK(callingUserPk, em));

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
