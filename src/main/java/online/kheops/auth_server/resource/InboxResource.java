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

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.PACSAuthTokenBuilder;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import org.dcm4che3.data.Tag;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public class InboxResource
{
    private static class Strings {
        private static final String StudyInstanceUID = "StudyInstanceUID";
        private static final String SeriesInstanceUID = "SeriesInstanceUID";
    }

    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    @Context
    ServletContext context;

    private void checkValidUID(String uid, String name) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(name + " is not a valid UID").build());
        }
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID}/users/{user}")
    public Response putStudy(@PathParam("user") String username,
                             @PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                             @Context SecurityContext securityContext) {

        LOG.info("studies/{StudyInstanceUID}/users/{user}");
        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.pk = :userPk", User.class);
            userQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            User callingUser = userQuery.setParameter("userPk", callingUserPk).getSingleResult();

            final User targetUser = User.findByUsername(username, em);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Unknown target user").build();
            }

            if (callingUserPk == targetUser.getPk()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Can't send a study to yourself").build();
            }

            // find all the series that the use has access to
            TypedQuery<Series> seriesQuery = em.createQuery("select s from Series s where :callingUser MEMBER OF s.users and s.study.studyInstanceUID = :StudyInstanceUID", Series.class);
            seriesQuery.setParameter("callingUser", callingUser);
            seriesQuery.setParameter(Strings.StudyInstanceUID, studyInstanceUID);
            seriesQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            List<Series> availableSeries = seriesQuery.getResultList();

            if (availableSeries.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No study with the given StudyInstanceUID").build();
            }

            for (Series series: availableSeries) {
                series.getUsers().add(targetUser);
                targetUser.getSeries().add(series);
                em.persist(series);
            }
            em.persist(targetUser);

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" with "+username);
        return Response.status(201).build();
    }


    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/users/{user}")
    public Response putSeries(@PathParam("user") String username,
                              @PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                              @PathParam(Strings.SeriesInstanceUID) String seriesInstanceUID,
                              @Context SecurityContext securityContext) {

        LOG.info("studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/users/{user}");
        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Strings.SeriesInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();


        try {
            tx.begin();

            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.pk = :pk", User.class);
            userQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            User callingUser = userQuery.setParameter("pk", callingUserPk).getSingleResult();

            User targetUser = User.findByUsername(username, em);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Unknown target user").build();
            }

            if (targetUser.getPk() == callingUserPk) { // the user is requesting access to a new series
                return Response.status(Response.Status.FORBIDDEN).entity("Use studies/{StudyInstanceUID}/series/{SeriesInstanceUID} for request access to a new series").build();
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
                    return Response.status(Response.Status.NOT_FOUND).entity("Unknown series").build();
                }

                if (series.getUsers().contains(targetUser)) {
                    LOG.info("Nothing to send, StudyInstanceUID:"+studyInstanceUID+", SeriesInstanceUID:"+seriesInstanceUID+" is accessible by "+username);
                    return Response.status(Response.Status.OK).build();
                }

                series.getUsers().add(targetUser);
                targetUser.getSeries().add(series);
                em.persist(series);
                em.persist(targetUser);
            }

            tx.commit();
            LOG.info("sending, StudyInstanceUID:"+studyInstanceUID+", SeriesInstanceUID:"+seriesInstanceUID+" to "+username);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return Response.status(201).build();
    }

@PUT
@Secured
@Path("studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
public Response putSeries(@PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                          @PathParam(Strings.SeriesInstanceUID) String seriesInstanceUID,
                          @Context SecurityContext securityContext) {

    LOG.info("studies/{StudyInstanceUID}/series/{SeriesInstanceUID}");

    checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);
    checkValidUID(seriesInstanceUID, Strings.SeriesInstanceUID);

    final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

    EntityManager em = EntityManagerListener.createEntityManager();
    EntityTransaction tx = em.getTransaction();


    try {
        tx.begin();

        TypedQuery<User> userQuery = em.createQuery("select u from User u where u.pk = :pk", User.class);
        userQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        User callingUser = userQuery.setParameter("pk", callingUserPk).getSingleResult();

        if (callingUser == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Unknown target user").build();
        }

        // find if the series already exists
        try {
            TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :SeriesInstanceUID", Series.class);
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            final Series storedSeries = query.setParameter(Strings.SeriesInstanceUID, seriesInstanceUID).getSingleResult();

            // we need to check here if the series that was found is owned by the user
            if (storedSeries.getUsers().contains(callingUser)) {
                LOG.info("Nothing to claim, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID + " is accessible by " + callingUser.getGoogleId());
                return Response.status(Response.Status.OK).build();
            } else if (storedSeries.getUsers().isEmpty()) {
                storedSeries.getUsers().add(callingUser);
                callingUser.getSeries().add(storedSeries);
                em.persist(storedSeries);
                em.persist(callingUser);
                tx.commit();
                LOG.info("Claim accepted because no one else owns the series, StudyInstanceUID:" + studyInstanceUID+", SeriesInstanceUID:" + seriesInstanceUID + " is not accessible by "+callingUser.getGoogleId());
                return Response.status(Response.Status.OK).build();
            } else {
                LOG.info("Claim denied, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:" + seriesInstanceUID + " is not accessible by "+callingUser.getGoogleId());
                return Response.status(Response.Status.FORBIDDEN).entity("Access to series denied").build();
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
        LOG.info("finished claiming, StudyInstanceUID:" + studyInstanceUID+", SeriesInstanceUID:"+seriesInstanceUID+" to " + callingUser.getGoogleId());

        tx.commit();
        LOG.info("sending, StudyInstanceUID:" + studyInstanceUID + ", SeriesInstanceUID:"+seriesInstanceUID+" to " + callingUser.getGoogleId());
    } finally {
        if (tx.isActive()) {
            tx.rollback();
        }
        em.close();
    }

    return Response.status(201).build();
}

    private Set<String> availableSeriesUIDs(long userPk, String studyInstanceUID) {
        Set<String> availableSeriesUIDs;

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User callingUser = User.findByPk(userPk, em);
            if (callingUser == null) {
                throw new IllegalStateException("calling user can't be found");
            }

            TypedQuery<String> query = em.createQuery("select s.seriesInstanceUID from Series s where s.study.studyInstanceUID = :StudyInstanceUID and :user member of s.users", String.class);
            query.setParameter(Strings.StudyInstanceUID, studyInstanceUID);
            query.setParameter("user", callingUser);
            availableSeriesUIDs = new HashSet<>(query.getResultList());

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return availableSeriesUIDs;
    }

    @GET
    @Secured
    @Path("studies/{StudyInstanceUID}/metadata")
    @Produces("application/dicom+json")
    public Response getStudiesMetadata(@PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                                       @Context SecurityContext securityContext) throws URISyntaxException {

        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        URI dicomWebURI = new URI(context.getInitParameter("online.kheops.pacs.uri"));
        UriBuilder uriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies/{StudyInstanceUID}/metadata");
        URI uri = uriBuilder.build(studyInstanceUID);
        String authToken = PACSAuthTokenBuilder.newBuilder().withStudyUID(studyInstanceUID).withAllSeries().build();
        Client client = ClientBuilder.newClient();
        InputStream is = client.target(uri).request("application/dicom+json").header("Authorization", "Bearer "+authToken).get(InputStream.class);

        JsonParser parser = Json.createParser(is);
        JSONReader jsonReader = new JSONReader(parser);


        Set<String> availableSeriesUIDs = this.availableSeriesUIDs(callingUserPk, studyInstanceUID);

        StreamingOutput stream = os -> {
            JsonGenerator generator = Json.createGenerator(os);
            JSONWriter jsonWriter = new JSONWriter(generator);

            generator.writeStartArray();

            try {
                jsonReader.readDatasets((fmi, dataset) -> {
                    if (availableSeriesUIDs.contains(dataset.getString(Tag.SeriesInstanceUID))) {
                        jsonWriter.write(dataset);
                    }
                });
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error while processing metadata", e);
                throw new WebApplicationException(e);
            }

            generator.writeEnd();
            generator.flush();
        };

        return Response.ok(stream).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID}")
    @Produces("application/dicom+json")
    public Response deleteStudy(@PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                                @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User targetUser = User.findByPk(callingUserPk, em);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Unknown target user").build();
            }

            TypedQuery<Series> query = em.createQuery("select s from Series s where s.study.studyInstanceUID = :StudyInstanceUID and :user member of s.users", Series.class);
            query.setParameter(Strings.StudyInstanceUID, studyInstanceUID);
            query.setParameter("user", targetUser);
            List<Series> seriesList = query.getResultList();

            if (seriesList.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No access to any series with the given studyInstanceUID").build();
            }

            for (Series series: seriesList) {
                series.getUsers().remove(targetUser);
                targetUser.getSeries().remove(series);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
    @Produces("application/dicom+json")
    public Response deleteSeries(@PathParam(Strings.StudyInstanceUID) String studyInstanceUID,
                                 @PathParam(Strings.SeriesInstanceUID) String seriesInstanceUID,
                                 @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Strings.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Strings.SeriesInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        EntityManager em = EntityManagerListener.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User targetUser = User.findByPk(callingUserPk, em);
            if (targetUser == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Unknown target user").build();
            }

            TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :SeriesInstanceUID and s.study.studyInstanceUID = :StudyInstanceUID and :user member of s.users", Series.class);
            query.setParameter(Strings.StudyInstanceUID, studyInstanceUID);
            query.setParameter(Strings.SeriesInstanceUID, seriesInstanceUID);
            query.setParameter("user", targetUser);
            List<Series> seriesList = query.getResultList();

            if (seriesList.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("User does not have access to any series with a study with the given studyInstanceUID").build();
            }

            for (Series series: seriesList) {
                series.getUsers().remove(targetUser);
                targetUser.getSeries().remove(series);
            }

            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
