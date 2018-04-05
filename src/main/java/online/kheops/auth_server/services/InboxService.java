package online.kheops.auth_server.services;


import javax.persistence.*;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import online.kheops.auth_server.Secured;
import online.kheops.auth_server.Series;
import online.kheops.auth_server.User;
import online.kheops.auth_server.UserStudy;

@Path("/users")
public class InboxService
{
    @PUT
    @Secured
    @Path("/{user}/studies/{studyInstanceUID}")
    public Response putStudy(@PathParam("user") String username,
                             @PathParam("studyInstanceUID") String studyInstanceUID,
                             @Context SecurityContext securityContext) {

        System.out.println(securityContext.getUserPrincipal());

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
    @Path("/{user}/studies/{studyInstanceUID}/series/{seriesInstanceUID}")
    public String putSeries(@PathParam("user") String user, @PathParam("studyInstanceUID") String studyInstanceUID, @PathParam("seriesInstanceUID") String seriesInstanceUID) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        // find the user


        // if the user doesn't exist blow out


        Series series = new Series(seriesInstanceUID);

        em.persist(series);

        tx.commit();
        em.close();;
        factory.close();


//        Series series = new Series(studyInstanceUID, seriesInstanceUID);
//        Session session = HibernateUtil.getSessionFactory().openSession();
//        session.beginTransaction();
//        session.save(series);
//        session.getTransaction().commit();
//        session.close();
        System.out.println("Done");

        String output = "Hi from putSeries: " + seriesInstanceUID + " studyInstanceIUD: " + studyInstanceUID + " for user: " + user;
        return output;
    }

}
