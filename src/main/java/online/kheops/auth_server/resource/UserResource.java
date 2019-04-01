package online.kheops.auth_server.resource;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.album.Albums;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.entity.Study;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.keycloak.Keycloak;
import online.kheops.auth_server.keycloak.KeycloakException;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.study.StudyQueries;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.study.StudyQueries.findStudyByStudyUID;
import static online.kheops.auth_server.user.UserPermissionEnum.LIST_USERS;
import static online.kheops.auth_server.user.UserQueries.findUserByUserId;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.StudyInstanceUID;

@Path("/")
public class UserResource {

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @GET
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(LIST_USERS)
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getUserInfo(@QueryParam("reference") String reference,
                                @QueryParam(ALBUM) String albumId,
                                @QueryParam(StudyInstanceUID) @UIDValidator String studyInstanceUID) {

        final UserResponseBuilder userResponseBuilder;

        try {
            final Keycloak keycloak = Keycloak.getInstance();
            userResponseBuilder = keycloak.getUser(reference);

            if(albumId != null) {
                userResponseBuilder.setAlbumAccess(Albums.isMemberOfAlbum(userResponseBuilder.build().getSub(), albumId));
            }

            if(studyInstanceUID != null) {
                userResponseBuilder.setStudyAccess(Studies.canAccessStudy(userResponseBuilder.build().getSub(), studyInstanceUID));
            }

            return Response.status(OK).entity(userResponseBuilder.build().getResponse()).build();
        } catch (UserNotFoundException e) {
            return Response.status(NO_CONTENT).build();
        } catch (KeycloakException e) {
            return Response.status(BAD_GATEWAY).entity(e.getMessage()).build();
        }
    }
}
