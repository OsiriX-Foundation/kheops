package online.kheops.auth_server.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import online.kheops.auth_server.HibernateUtil;
import online.kheops.auth_server.Person;
import org.hibernate.Session;

@Path("/user")
public class UserService {

    @GET
    @Path("/{name}")
    public String sayHello(@PathParam("name") String name) {

        Person person = new Person();
        person.setId(1);
        person.setName(name);
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(person);
        session.getTransaction().commit();
        session.close();
        System.out.println("Done");

        String output = "Hi from Jersey REST Service: " + name;
        return output;
    }
}
