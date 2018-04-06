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
        em.close();;
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
            throw new BadRequestException("Invalidly formed studyInstanceUID");
        }
        try {
            new Oid(seriesInstanceUID);
        } catch (GSSException exception) {
            throw new BadRequestException("Invalidly formed seriesInstanceUID");
        }

        final String callerUsername = securityContext.getUserPrincipal().getName();
        // is the user sharing a series, or requesting access to a new series

        if (callerUsername.equals(username)) { // the user is requesting access to a new series
            EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
            EntityManager em = factory.createEntityManager();
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();

                // find if the series already exists
                try {
                    TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :seriesInstanceUID", Series.class);
                    query.setParameter("seriesInstanceUID", seriesInstanceUID).getSingleResult();
                    throw new ForbiddenException("Series already known");
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
//                try {
//                    TypedQuery<UserStudy> query = em.createQuery("select us from UserStudy us where us.study = :studyInstanceUID", Study.class);
//                    study = query.setParameter("studyInstanceUID", studyInstanceUID).getSingleResult();
//
//
                tx.commit();
            } finally {
                em.close();
                factory.close();
            }

        }

        return Response.ok().build();
    }
}
