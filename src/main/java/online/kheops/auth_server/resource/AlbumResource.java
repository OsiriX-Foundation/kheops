package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.*;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.AlbumAccessSecured;
import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.annotation.UserAccessSecured;

import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;


@Path("/")
public class AlbumResource {

    private static final Logger LOG = Logger.getLogger(AlbumResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @POST
    @Secured
    @UserAccessSecured
    @Path("album")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newAlbum(@DefaultValue("Album_name") @FormParam("name") String name, @DefaultValue("") @FormParam("description") String description,
                             @FormParam("addUser") Boolean addUser, @FormParam("downloadSeries") Boolean downloadSeries,
                             @FormParam("sendSeries") Boolean sendSeries, @FormParam("deleteSeries") Boolean deleteSeries,
                             @FormParam("addSeries") Boolean addSeries, @FormParam("writeComments") Boolean writeComments,
                             @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        final UsersPermission usersPermission = new UsersPermission();

        if (addUser != null) { usersPermission.setAddUser(addUser); }
        if (downloadSeries != null) { usersPermission.setDownloadSeries(downloadSeries); }
        if (sendSeries != null){ usersPermission.setSendSeries(sendSeries); }
        if (deleteSeries != null) { usersPermission.setDeleteSeries(deleteSeries); }
        if (addSeries != null) { usersPermission.setAddSeries(addSeries); }
        if (writeComments != null) { usersPermission.setWriteComments(writeComments); }

        final AlbumResponses.AlbumResponse albumResponse;

        try {
            albumResponse = Albums.createAlbum(kheopsPrincipal.getUser(), name, description, usersPermission);
        } catch (JOOQException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        LOG.info("New album pk:"+albumResponse.id+" created by user pk:"+kheopsPrincipal.getUser().getGoogleEmail());
        return Response.status(CREATED).entity(albumResponse).build();
    }

    @GET
    @Secured
    @Path("album")
    @UserAccessSecured
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getAlbums(@Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        final PairListXTotalCount<AlbumResponses.AlbumResponse> pairAlbumsTotalAlbum;

        try {
            pairAlbumsTotalAlbum = Albums.getAlbumList(kheopsPrincipal.getDBID(), uriInfo.getQueryParameters());
        } catch (UserNotFoundException e) {
            LOG.log(Level.INFO, "Get albums list by user pk:"+callingUserPk+" FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (JOOQException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (BadQueryParametersException e) {
            LOG.log(Level.INFO, e.getMessage(), e);
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        final GenericEntity<List<AlbumResponses.AlbumResponse>> genericAlbumResponsesList = new GenericEntity<List<AlbumResponses.AlbumResponse>>(pairAlbumsTotalAlbum.getAttributesList()) {};
        return Response.ok(genericAlbumResponsesList)
                .header(X_TOTAL_COUNT, pairAlbumsTotalAlbum.getXTotalCount())
                .build();
    }

    @GET
    @Secured
    @AlbumAccessSecured
    @Path("album/{album:[1-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlbum(@PathParam("album") Long albumPk, @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        final AlbumResponses.AlbumResponse albumResponse;

        try {
            albumResponse = Albums.getAlbum(callingUserPk, albumPk, kheopsPrincipal.hasUserAccess());
        } catch (JOOQException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        return Response.status(OK).entity(albumResponse).build();
    }

    @PATCH
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UsersPermission.UsersPermissionEnum.EDIT_ALBUM)
    @Path("album/{album:[1-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editAlbum(@PathParam("album") Long albumPk,
                              @FormParam("name") String name, @FormParam("description") String description,
                              @FormParam("addUser") Boolean addUser, @FormParam("downloadSeries") Boolean downloadSeries,
                              @FormParam("sendSeries") Boolean sendSeries, @FormParam("deleteSeries") Boolean deleteSeries,
                              @FormParam("addSeries") Boolean addSeries, @FormParam("writeComments") Boolean writeComments,
                              @FormParam("notificationNewSeries") Boolean notificationNewSeries,
                              @FormParam("notificationNewComment") Boolean notificationNewComment,
                              @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        final AlbumResponses.AlbumResponse albumResponse;
        final UsersPermission usersPermission = new UsersPermission();

        usersPermission.setAddUser(addUser);
        usersPermission.setDownloadSeries(downloadSeries);
        usersPermission.setSendSeries(sendSeries);
        usersPermission.setDeleteSeries(deleteSeries);
        usersPermission.setAddSeries(addSeries);
        usersPermission.setWriteComments(writeComments);

        try {
            albumResponse = Albums.editAlbum(callingUserPk, albumPk, name, description, usersPermission, notificationNewSeries, notificationNewComment);
        } catch (UserNotFoundException | AlbumNotFoundException | UserNotMemberException e) {
            LOG.log(Level.INFO, "Edit album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Edit album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(FORBIDDEN).entity(e.getMessage()).build();
        } catch (JOOQException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return Response.status(INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        return Response.status(OK).entity(albumResponse).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UsersPermission.UsersPermissionEnum.DELETE_ALBUM)
    @Path("album/{album:[1-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteAlbum(@PathParam("album") Long albumPk,@Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Albums.deleteAlbum(callingUserPk, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Delete album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info("Delete album pk:" +albumPk+  " by user pk:"+callingUserPk+ " SUCCESS");
        return Response.status(NO_CONTENT).build();
    }

    @GET
    @Secured
    @AlbumAccessSecured
    @UserAccessSecured
    @AlbumPermissionSecured(UsersPermission.UsersPermissionEnum.LIST_USERS)
    @Path("album/{album:[1-9][0-9]*}/users")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersAlbum(@PathParam("album") Long albumPk,@Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        final List<AlbumResponses.UserAlbumResponse> usersAlbumResponse;

        try {
            usersAlbumResponse = Albums.getUsers(callingUserPk, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Get users list for album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final GenericEntity<List<AlbumResponses.UserAlbumResponse>> genericUsersAlbumResponsesList = new GenericEntity<List<AlbumResponses.UserAlbumResponse>>(usersAlbumResponse) {};
        return Response.status(OK).entity(genericUsersAlbumResponsesList).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UsersPermission.UsersPermissionEnum.ADD_USER)
    @Path("album/{album:[1-9][0-9]*}/users/{user}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addUser(@PathParam("album") Long albumPk, @PathParam("user") String user,
                            @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Albums.addUser(callingUserPk, user, albumPk, false);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Add an user userName:"+user+" to the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Add an user userName:"+user+" to the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(FORBIDDEN).entity(e.getMessage()).build();
        }

        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UsersPermission.UsersPermissionEnum.ADD_ADMIN)
    @Path("album/{album:[1-9][0-9]*}/users/{user}/admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addAdmin(@PathParam("album") Long albumPk, @PathParam("user") String user,
                             @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Albums.addUser(callingUserPk, user, albumPk, true);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Add an admin userName:"+user+" to the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Add an admin userName:"+user+" to the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(FORBIDDEN).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UsersPermission.UsersPermissionEnum.REMOVE_ADMIN)
    @Path("album/{album:[1-9][0-9]*}/users/{user}/admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeAdmin(@PathParam("album") Long albumPk, @PathParam("user") String user,
                                @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Albums.removeAdmin(callingUserPk, user, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException | UserNotMemberException e) {
            LOG.log(Level.INFO, "Remove an admin userName:"+user+" from the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UsersPermission.UsersPermissionEnum.REMOVE_USER)
    @Path("album/{album:[1-9][0-9]*}/users/{user}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteUser(@PathParam("album") Long albumPk, @PathParam("user") String user,
                               @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Albums.deleteUser(callingUserPk, user, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException | UserNotMemberException e) {
            LOG.log(Level.INFO, "Remove an user userName:"+user+" from the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Remove an user userName:"+user+" from the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(FORBIDDEN).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @AlbumAccessSecured
    @UserAccessSecured
    @Path("album/{album:[1-9][0-9]*}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addFavorites(@PathParam("album") Long albumPk, @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Albums.setFavorites(callingUserPk, albumPk, true);
        } catch (UserNotFoundException | AlbumNotFoundException | UserNotMemberException e) {
            LOG.log(Level.INFO,"Add an album pk:" +albumPk+ " to favorites by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @UserAccessSecured
    @Path("album/{album:[1-9][0-9]*}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteFavorites(@PathParam("album") Long albumPk, @Context SecurityContext securityContext) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Albums.setFavorites(callingUserPk, albumPk, false);
        } catch (UserNotFoundException | AlbumNotFoundException | UserNotMemberException e) {
            LOG.log(Level.INFO, "Remove an album pk:" +albumPk+ " from favorites by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }
}
