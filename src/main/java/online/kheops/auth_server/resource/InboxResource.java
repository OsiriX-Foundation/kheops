package online.kheops.auth_server.resource;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import java.util.List;

@Path("/users")
public class InboxResource
{
    @PUT
    @Secured
    @Path("/{user}/studies/{studyInstanceUID}")
    public Response putStudy(@PathParam("user") String username,
                             @PathParam("studyInstanceUID") String studyInstanceUID,
                             @Context SecurityContext securityContext) {

        // this call can only be used to send.

        // validate the UIDs
        try {
            new Oid(studyInstanceUID);
        } catch (GSSException exception) {
            return Response.status(400, "StudyInstanceUID is not a valid UID").build();
        }

        final String callingUsername = securityContext.getUserPrincipal().getName();

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        if (callingUsername.equals(username)) {
            return Response.status(400, "Can't send a study to yourself").build();
        }

        try {
            tx.begin();

            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.username = :username", User.class);
            userQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            User callingUser = userQuery.setParameter("username", callingUsername).getSingleResult();

            final User targetUser;
            try {
                targetUser = userQuery.setParameter("username", username).getSingleResult();
            } catch (Exception exception) {
                return Response.status(404, "Unknown target user").build();
            }

            // find all the series that the use has access to
            List<Series> availableSeries = null;
            try {
                TypedQuery<Series> seriesQuery = em.createQuery("select s from Series s where :callingUser MEMBER OF s.users and s.study.studyInstanceUID = :studyInstanceUID", Series.class);
                seriesQuery.setParameter("callingUser", callingUser);
                seriesQuery.setParameter("studyInstanceUID", studyInstanceUID);
                seriesQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
                availableSeries = seriesQuery.getResultList();
            } catch (Exception exception) {
                return Response.status(404, "No study with the given studyInstanceUID").build();
            }

            if (availableSeries == null || availableSeries.size() == 0) {
                return Response.status(404, "No study with the given studyInstanceUID").build();
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
    @Path("/{user}/studies/{studyInstanceUID}/series/{seriesInstanceUID}")
    public Response putSeries(@PathParam("user") String username,
                             @PathParam("studyInstanceUID") String studyInstanceUID,
                             @PathParam("seriesInstanceUID") String seriesInstanceUID,
                             @Context SecurityContext securityContext) {
        // validate the UIDs
        try {
            new Oid(studyInstanceUID);
        } catch (GSSException exception) {
            return Response.status(400, "StudyInstanceUID is not a valid UID").build();
        }
        try {
            new Oid(seriesInstanceUID);
        } catch (GSSException exception) {
            return Response.status(400, "SeriesInstanceUID is not a valid UID").build();
        }

        final String callingUsername = securityContext.getUserPrincipal().getName();
        // is the user sharing a series, or requesting access to a new series

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();


        try {
            tx.begin();

            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.username = :username", User.class);
            userQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            User callingUser = userQuery.setParameter("username", callingUsername).getSingleResult();

            if (callingUsername.equals(username)) { // the user is requesting access to a new series

                // find if the series already exists
                try {
                    TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :seriesInstanceUID", Series.class);
                    query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
                    final Series storedSeries = query.setParameter("seriesInstanceUID", seriesInstanceUID).getSingleResult();

                    // we need to check here if the series that was found it owned by the user
                    if (storedSeries.getUsers().contains(callingUser)) {
                        return Response.status(200, "User already has access to the series").build();
                    } else {
                        return Response.status(403, "Access to series denied").build();
                    }
                 } catch (NoResultException ignored) {}

                // find if the study already exists
                Study study = null;
                try {
                    TypedQuery<Study> query = em.createQuery("select s from Study s where s.studyInstanceUID = :studyInstanceUID", Study.class);
                    study = query.setParameter("studyInstanceUID", studyInstanceUID).getSingleResult();
                } catch (NoResultException ignored) {}

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
                final User targetUser;
                try {
                    targetUser = userQuery.setParameter("username", username).getSingleResult();
                } catch (Exception exception) {
                    return Response.status(404, "Unknown target user").build();
                }

                final Series series;
                try {
                    TypedQuery<Series> seriesQuery = em.createQuery("select s from Series s where s.study.studyInstanceUID = :studyInstanceUID and s.seriesInstanceUID = :seriesInstanceUID and :callingUser member of s.users", Series.class);
                    seriesQuery.setParameter("studyInstanceUID", studyInstanceUID);
                    seriesQuery.setParameter("seriesInstanceUID", seriesInstanceUID);
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
    public Response getSstudies(@PathParam("user") String username,
                                @Context SecurityContext securityContext) {

        // for now don't use any parameters

        // get a list of all the studies
        final String callingUsername = securityContext.getUserPrincipal().getName();
        // is the user sharing a series, or requesting access to a new series

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.username = :username", User.class);
            userQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            User callingUser = userQuery.setParameter("username", callingUsername).getSingleResult();

            TypedQuery<Study> studyQuery = em.createQuery("select distinct s.study from Series s where :callingUser member of s.users", Study.class);
            studyQuery.setParameter("callingUser", callingUser);
            studyQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);

            List<Study> userStudies = studyQuery.getResultList();

            if (userStudies != null && userStudies.size() > 0) {
                System.out.println(userStudies);
            }


            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        return Response.status(201).build();
    }

}
