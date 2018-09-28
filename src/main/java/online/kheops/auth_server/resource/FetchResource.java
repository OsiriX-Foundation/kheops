package online.kheops.auth_server.resource;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.fetch.Fetcher;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/")
public class FetchResource {

    @POST
    @Secured
    @Path("studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/fetch")
    public void getStudies(@PathParam("studyInstanceUID") String studyInstanceUID,
                               @Context SecurityContext securityContext) {
        checkValidUID(studyInstanceUID);
        Fetcher.fetchStudy(studyInstanceUID);
    }

    private void checkValidUID(String uid) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("StudyInstanceUID is not a valid UID").build());
        }
    }
}
