package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponses;
import online.kheops.auth_server.user.Users;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.user.UserResponses.userToUserResponse;

@Path("/")
public class UserRessource {

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @GET
    @Secured
    @UserAccessSecured
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getAlbums(@QueryParam("reference") String reference ) {

        final UserResponses.UserResponse userResponse;

        try {
            final User user = Users.getUser(reference);
            userResponse = userToUserResponse(user);
        } catch (UserNotFoundException e) {
            return Response.status(NO_CONTENT).build();
        }

        return Response.status(OK).entity(userResponse).build();
    }
}
