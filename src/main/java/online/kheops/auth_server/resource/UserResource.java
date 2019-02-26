package online.kheops.auth_server.resource;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static javax.ws.rs.core.Response.Status.*;

@Path("/")
public class UserResource {

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

        final UserResponse userResponse;

        try {
            final Keycloak keycloak = Keycloak.getInstance();
            userResponse = keycloak.getUser(reference);
            return Response.status(OK).entity(userResponse.getResponse()).build();
        } catch (UserNotFoundException e) {
            return Response.status(NO_CONTENT).build();
        } catch (KeycloakException e) {
            return Response.status(BAD_GATEWAY).entity(e.getMessage()).build();
        }
    }
}
