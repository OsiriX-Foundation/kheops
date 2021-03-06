package online.kheops.auth_server.resource;

import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.inbox.Inbox;
import online.kheops.auth_server.inbox.InboxInfoResponse;
import online.kheops.auth_server.principal.KheopsPrincipal;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static javax.ws.rs.core.Response.Status.*;


@Path("/")
public class InboxResource {

    @Context
    private SecurityContext securityContext;

    @GET
    @Secured
    @UserAccessSecured
    @Path("inboxinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inboxInfo() {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final InboxInfoResponse inboxInfoResponse;

        inboxInfoResponse = Inbox.getInboxInfo(kheopsPrincipal.getUser(), kheopsPrincipal.getKheopsLogBuilder());

        return Response.status(OK).entity(inboxInfoResponse).build();
    }
}
