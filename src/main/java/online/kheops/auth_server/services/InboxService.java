package online.kheops.auth_server.services;


import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import online.kheops.auth_server.HibernateUtil;
import online.kheops.auth_server.Series;
import org.hibernate.Session;

@Path("/users")
public class InboxService
{

    @PUT
    @Path("/{user}/studies/{studyInstanceUID}")
    public String putStudy(@PathParam("user") String user, @PathParam("studyInstanceUID") String studyInstanceUID) {

//        Person person = new Person();
//        person.setId(1);
//        person.setName(name);
//        Session session = HibernateUtil.getSessionFactory().openSession();
//        session.beginTransaction();
//        session.save(person);
//        session.getTransaction().commit();
//        session.close();
//        System.out.println("Done");

        String output = "Hi from putStudy: " + studyInstanceUID + " for user: " + user;
        return output;
    }

    @PUT
    @Path("/{user}/studies/{studyInstanceUID}/series/{seriesInstanceUID}")
    public String putSeries(@PathParam("user") String user, @PathParam("studyInstanceUID") String studyInstanceUID, @PathParam("seriesInstanceUID") String seriesInstanceUID) {

        Series series = new Series(studyInstanceUID, seriesInstanceUID);
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(series);
        session.getTransaction().commit();
        session.close();
        System.out.println("Done");

        String output = "Hi from putSeries: " + seriesInstanceUID + " studyInstanceIUD: " + studyInstanceUID + " for user: " + user;
        return output;
    }

}
