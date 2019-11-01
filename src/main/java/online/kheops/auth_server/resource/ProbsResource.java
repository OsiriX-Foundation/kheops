package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

@Path("/")
public class ProbsResource {
    @GET
    @Path("liveness")
    public Response liveness() {
        return Response.status(OK).build();
    }

    @GET
    @Path("readiness")
    public Response readiness() {

        final EntityManager em = EntityManagerListener.createEntityManager();

        try {
            //try to querying the db
            em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
        } catch (Exception e) {
            return Response.status(INTERNAL_SERVER_ERROR).entity("").build();
        } finally {
            em.close();
        }

        return Response.status(OK).build();
    }
}
