package online.kheops.auth_server.resource;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.fetch.Fetcher;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Path("/")
public class FetchResource {
    private static final Logger LOG = Logger.getLogger(FetchResource.class.getName());


    @POST
    @Secured
    @Path("studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/fetch")
    public Response getStudies(@PathParam("studyInstanceUID") String studyInstanceUID,
                               @Context SecurityContext securityContext) {
        LOG.log(Level.WARNING, "Fetch resource called for StudyUID:" + studyInstanceUID);
        checkValidUID(studyInstanceUID);
        Fetcher.fetchStudy(studyInstanceUID);
        return Response.ok().build();
    }

    private void checkValidUID(String uid) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new WebApplicationException(Response.status(BAD_REQUEST).entity("StudyInstanceUID is not a valid UID").build());
        }
    }
}
