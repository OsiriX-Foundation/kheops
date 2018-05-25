package online.kheops.auth_server.resource;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.persistence.*;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.*;

import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.PersistenceUtils;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/users")
public class InboxResource
{
    private static class Strings {
        private static final String StudyInstanceUID = "StudyInstanceUID";
        private static final String SeriesInstanceUID = "SeriesInstanceUID";
    }

    @Context
    ServletContext context;

    private void checkValidUID(String uid, String name) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new WebApplicationException(Response.status(400, name + " is not a valid UID").build());
        }
    }

    @PUT
    @Secured
    @Path("/{user}/studies/{StudyInstanceUID}")
    public Response putStudy(@PathParam("user") String username,
                             @PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                             @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.pk = :userPk", User.class);
            userQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            User callingUser = userQuery.setParameter("userPk", callingUserPk).getSingleResult();

            final User targetUser = User.findByUsername(username, em);

            if (callingUserPk == targetUser.getPk()) {
                return Response.status(400, "Can't send a study to yourself").build();
            }

            // find all the series that the use has access to
            List<Series> availableSeries;
            try {
                TypedQuery<Series> seriesQuery = em.createQuery("select s from Series s where :callingUser MEMBER OF s.users and s.study.studyInstanceUID = :StudyInstanceUID", Series.class);
                seriesQuery.setParameter("callingUser", callingUser);
                seriesQuery.setParameter(Strings.StudyInstanceUID, studyInstanceUID);
                seriesQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
                availableSeries = seriesQuery.getResultList();
            } catch (Exception exception) {
                return Response.status(404, "No study with the given StudyInstanceUID").build();
            }

            if (availableSeries == null || availableSeries.size() == 0) {
                return Response.status(404, "No study with the given StudyInstanceUID").build();
            }

            for (Series series: availableSeries) {
                series.getUsers().add(targetUser);
                targetUser.getSeries().add(series);
                em.persist(series);
            }
            em.persist(targetUser);

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        return Response.status(201).build();
    }


    @PUT
    @Secured
    @Path("/{user}/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    public Response putSeries(@PathParam("user") String username,
                             @PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                             @PathParam(Strings.SeriesInstanceUID) String seriesInstanceUID,
                             @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Strings.SeriesInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();


        try {
            tx.begin();

            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.pk = :pk", User.class);
            userQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            User callingUser = userQuery.setParameter("pk", callingUserPk).getSingleResult();

            User targetUser;
            try {
                targetUser = User.findByUsername(username, em);
            } catch (NoResultException exception) {
                return Response.status(404, "Unknown target user").build();
            }

            if (targetUser.getPk() == callingUserPk) { // the user is requesting access to a new series

                // find if the series already exists
                try {
                    TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
                    query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
                    final Series storedSeries = query.setParameter(Strings.SeriesInstanceUID, seriesInstanceUID).getSingleResult();

                    // we need to check here if the series that was found it owned by the user
                    if (storedSeries.getUsers().contains(callingUser)) {
                        return Response.status(200, "User already has access to the series").build();
                    } else {
                        return Response.status(403, "Access to series denied").build();
                    }
                 } catch (NoResultException ignored) {/*empty*/}

                // find if the study already exists
                Study study = null;
                try {
                    TypedQuery<Study> query = em.createQuery("select s from Study s where s.studyInstanceUID = :StudyInstanceUID", Study.class);
                    study = query.setParameter(Strings.StudyInstanceUID, studyInstanceUID).getSingleResult();
                } catch (NoResultException ignored) {/*empty*/}

                if (study == null) { // the study doesn't exist, we need to create it
                    study = new Study();
                    study.setStudyInstanceUID(studyInstanceUID);
                    em.persist(study);
                }

                Series series = new Series(seriesInstanceUID);
                series.getUsers().add(callingUser);
                callingUser.getSeries().add(series);
                study.getSeries().add(series);

                em.persist(study);
                em.persist(series);
                em.persist(callingUser);
            } else { // the user wants to share
                final Series series;
                try {
                    TypedQuery<Series> seriesQuery = em.createQuery("select s from Series s where s.study.studyInstanceUID = :StudyInstanceUID and s.seriesInstanceUID = :SeriesInstanceUID and :callingUser member of s.users", Series.class);
                    seriesQuery.setParameter(Strings.StudyInstanceUID, studyInstanceUID);
                    seriesQuery.setParameter(Strings.SeriesInstanceUID, seriesInstanceUID);
                    seriesQuery.setParameter("callingUser", callingUser);
                    seriesQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
                    series = seriesQuery.getSingleResult();
                } catch (Exception exception) {
                    return Response.status(404, "Unknown series").build();
                }

                if (series.getUsers().contains(targetUser)) {
                    return Response.status(200, "User already has access to the series").build();
                }

                series.getUsers().add(targetUser);
                targetUser.getSeries().add(series);
                em.persist(series);
                em.persist(targetUser);
            }

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        return Response.status(201).build();
    }

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

        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        List<Attributes> attributesList;

        try {
            tx.begin();

            long targetUserPk = User.findPkByUsername(username, em);
            if (callingUserPk != targetUserPk) {
                return Response.status(403, "Access to study denied").build();
            }

            attributesList = new ArrayList<>(Study.findAttributesByUserPK(callingUserPk, em));

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        GenericEntity<List<Attributes>> genericAttributesList = new GenericEntity<List<Attributes>>(attributesList) {};
        return Response.ok(genericAttributesList).build();
    }

    private Set<String> availableSeriesUIDs(long userPk, String studyInstanceUID) {
        Set<String> availableSeriesUIDs = new HashSet<>();

        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User callingUser = User.findByPk(userPk, em);

            TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from Series s where s.study.studyInstanceUID = :StudyInstanceUID and :user member of s.users", String.class);
            query.setParameter(Strings.StudyInstanceUID, studyInstanceUID);
            query.setParameter("user", callingUser);
            availableSeriesUIDs.addAll(query.getResultList());

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        return availableSeriesUIDs;
    }

    @GET
    @Secured
    @Path("/{user}/studies/{StudyInstanceUID}/metadata")
    @Produces("application/dicom+json")
    public Response getStudiesMetadata(@PathParam("user") String username,
                                       @PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                                       @Context SecurityContext securityContext) throws URISyntaxException {

        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        long targetUserPk;
        try {
            tx.begin();

            targetUserPk = User.findPkByUsername(username, em);

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        if (callingUserPk != targetUserPk) {
            return Response.status(403, "Access to study denied").build();
        }

        URI dicomWebURI = new URI(context.getInitParameter("online.kheops.pacs.uri"));
        UriBuilder uriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies/{StudyInstanceUID}/metadata");
        URI uri = uriBuilder.build(studyInstanceUID);
        Client client = ClientBuilder.newClient();
        InputStream is = client.target(uri).request("application/dicom+json").get(InputStream.class);

        JsonParser parser = Json.createParser(is);
        JSONReader jsonReader = new JSONReader(parser);


        Set<String> availableSeriesUIDs = this.availableSeriesUIDs(((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID(), studyInstanceUID);

        StreamingOutput stream = os -> {
            JsonGenerator generator = Json.createGenerator(os);
            JSONWriter jsonWriter = new JSONWriter(generator);

            generator.writeStartArray();

            jsonReader.readDatasets((fmi, dataset) -> {
                if (availableSeriesUIDs.contains(dataset.getString(Tag.SeriesInstanceUID))) {
                    jsonWriter.write(dataset);
                }
            });

            generator.writeEnd();
            generator.flush();
        };

        return Response.ok(stream).build();
    }

    @DELETE
    @Secured
    @Path("/{user}/studies/{StudyInstanceUID}")
    @Produces("application/dicom+json")
    public Response deleteStudy(@PathParam("user") String username,
                                @PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                                @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User targetUser = User.findByUsername(username, em);
            if (callingUserPk != targetUser.getPk()) {
                return Response.status(403, "Access to study denied").build();
            }

            TypedQuery<Series> query = em.createQuery("select s from Series s where s.study.studyInstanceUID = :StudyInstanceUID and :user member of s.users", Series.class);
            query.setParameter(Strings.StudyInstanceUID, studyInstanceUID);
            query.setParameter("user", targetUser);
            List<Series> seriesList = query.getResultList();

            for (Series series: seriesList) {
                series.getUsers().remove(targetUser);
                targetUser.getSeries().remove(series);
            }

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
