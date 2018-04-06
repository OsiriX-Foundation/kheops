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
import online.kheops.auth_server.entity.UserStudy;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

@Path("/users")
public class InboxResource
{
    @PUT
    @Secured
    @Path("/{user}/studies/{studyInstanceUID}")
    public Response putStudy(@PathParam("user") String username,
                             @PathParam("studyInstanceUID") String studyInstanceUID,
                             @Context SecurityContext securityContext) {

        System.out.println(securityContext.getUserPrincipal().getName());

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        User user = null;
        try {
            TypedQuery<User> query = em.createQuery("select u from User u where u.username = :username", User.class);
            user = query.setParameter("username", username).getSingleResult();
        } catch (Exception exception) {
            System.out.println("User not found");
        }

        if (user == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }


        UserStudy userStudy = new UserStudy();
        user.getUserStudies().add(userStudy);

        em.persist(user);
        em.persist(userStudy);

        tx.commit();
        em.close();
        factory.close();

        String output = "Hi from putStudy: " + studyInstanceUID + " for user: " + user;
        return Response.ok(output).build();
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

        TypedQuery<User> userQuery = em.createQuery("select u from User u where u.username = :username", User.class);
        User callingUser = userQuery.setParameter("username", callingUsername).getSingleResult();

        try {
            tx.begin();

            if (callingUsername.equals(username)) { // the user is requesting access to a new series

                // find if the series already exists
                try {
                    TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :seriesInstanceUID", Series.class);
                    final Series storedSeries = query.setParameter("seriesInstanceUID", seriesInstanceUID).getSingleResult();
                    // we need to check here if the series that was found it owned by the user

                    TypedQuery<UserStudy> userStudyQuery = em.createQuery("select us from UserStudy us where :series MEMBER OF us.series and us.user = :user AND us.study.studyInstanceUID = :studyInstanceUID", UserStudy.class);
                    userStudyQuery.setParameter("series", storedSeries);
                    userStudyQuery.setParameter("user", callingUser);
                    userStudyQuery.setParameter("studyInstanceUID", studyInstanceUID);
                    try {
                        userStudyQuery.getSingleResult();
                    } catch (Exception exception) {
                        return Response.status(403, "Access to series denied").build();
                    }
                    return Response.status(200, "User already has access to the series").build();
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
                study.getSeries().add(series);

                em.persist(study);
                em.persist(series);

                // find if the user has a userStudy for this study
                UserStudy userStudy = null;
                try {
                    TypedQuery<UserStudy> query = em.createQuery("select userStudy from UserStudy userStudy where userStudy.study = :study and userStudy.user = :user", UserStudy.class);
                    query.setParameter("study", study);
                    userStudy = query.setParameter("user", callingUser).getSingleResult();
                } catch (NoResultException ignored) {}

                if (userStudy == null) {
                    userStudy = new UserStudy();
                    userStudy.setStudy(study);
                    callingUser.getUserStudies().add(userStudy);
                }
                userStudy.getSeries().add(series);
                em.persist(userStudy);
            } else { // the user wants to share
                final User targetUser;
                try {
                    targetUser = userQuery.setParameter("username", username).getSingleResult();
                } catch (Exception exception) {
                    return Response.status(400, "Unknown target user").build();
                }

                // does the Study even exist
                final Study study;
                try {
                    TypedQuery<Study> studyQuery = em.createQuery("select s from Study s where s.studyInstanceUID = :studyInstanceUID", Study.class);
                    studyQuery.setParameter("studyInstanceUID", studyInstanceUID);
                    study = studyQuery.getSingleResult();
                } catch (Exception exception) {
                    return Response.status(400, "No study with the given studyInstanceUID").build();
                }

                final Series series;
                try {
                    TypedQuery<Series> seriesQuery = em.createQuery("select s from Series s where s.study = :study and s.seriesInstanceUID = :seriesInstanceUID", Series.class);
                    seriesQuery.setParameter("seriesInstanceUID", seriesInstanceUID);
                    seriesQuery.setParameter("study", study);
                    series = seriesQuery.getSingleResult();
                } catch (Exception exception) {
                    return Response.status(400, "Unknown series").build();
                }

                // find if a UserStudy
                final UserStudy callingUserStudy;
                try {
                    TypedQuery<UserStudy> userStudyQuery = em.createQuery("select userStudy from UserStudy userStudy where userStudy.study = :study and :series member of userStudy.series and userStudy.user = :user", UserStudy.class);
                    userStudyQuery.setParameter("study", study);
                    userStudyQuery.setParameter("user", callingUser);
                    userStudyQuery.setParameter("series", series);
                    callingUserStudy = userStudyQuery.getSingleResult();
                } catch (Exception exception) {
                    return Response.status(400, "The user does not have access to the series").build();
                }

                UserStudy targetUserStudy = null;
                try {
                    TypedQuery<UserStudy> userStudyQuery = em.createQuery("select userStudy from UserStudy userStudy where userStudy.study = :study and userStudy.user = :user", UserStudy.class);
                    userStudyQuery.setParameter("study", study);
                    userStudyQuery.setParameter("user", targetUser);
                    targetUserStudy = userStudyQuery.getSingleResult();
                } catch (NoResultException ignored) {}

                if (targetUserStudy == null) {
                    targetUserStudy = new UserStudy();
                    targetUserStudy.setStudy(study);
                    targetUser.getUserStudies().add(targetUserStudy);
                }

                if (targetUserStudy.getSeries().contains(series)) {
                    return Response.status(200, "The target user already has access to the series").build();
                }

                targetUserStudy.getSeries().add(series);
                em.persist(targetUserStudy);
            }

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        return Response.status(201).build();
    }
}
