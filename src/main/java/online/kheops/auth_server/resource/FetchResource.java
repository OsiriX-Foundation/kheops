package online.kheops.auth_server.resource;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.fetch.Fetcher;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Path("/")
public class FetchResource {

    @POST
    @Secured
    @Path("studies/{studyInstanceUID:([1-9][0-9]*[.])*[1-9][0-9]*}/fetch")
    public void getStudies(@PathParam("studyInstanceUID") String studyInstanceUID,
                               @Context SecurityContext securityContext) {
        Fetcher.fetchStudy(studyInstanceUID);
    }

}
