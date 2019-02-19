package online.kheops.auth_server.resource;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UIDValidator;
import online.kheops.auth_server.fetch.Fetcher;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.util.Consts.StudyInstanceUID;

@Path("/")
public class FetchResource {
    private static final Logger LOG = Logger.getLogger(FetchResource.class.getName());

    @POST
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/fetch")
    public Response getStudies(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                               @Context SecurityContext securityContext) {
        LOG.log(Level.WARNING,() ->  "Fetch resource called for StudyUID:" + studyInstanceUID);
        Fetcher.fetchStudy(studyInstanceUID);
        return Response.ok().build();
    }
}
