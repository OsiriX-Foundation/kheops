package online.kheops.auth_server.resource;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UIDValidator;
import online.kheops.auth_server.fetch.Fetcher;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.util.KheopsLogBuilder.*;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static online.kheops.auth_server.util.Consts.StudyInstanceUID;

@Path("/")
public class FetchResource {

    @Context
    private SecurityContext securityContext;

    @POST
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/fetch")
    public Response getStudies(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                               @Context SecurityContext securityContext) {
        Fetcher.fetchStudy(studyInstanceUID);
        ((KheopsPrincipalInterface) securityContext.getUserPrincipal()).getKheopsLogBuilder()
                .study(studyInstanceUID)
                .action(ActionType.FETCH)
                .log();
        return Response.ok().build();
    }
}
