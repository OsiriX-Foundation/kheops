package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.*;
import online.kheops.auth_server.annotation.AlbumAccessSecured;
import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.util.Consts.DbColumnSize;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.ErrorResponse.ErrorResponseBuilder;
import online.kheops.auth_server.util.KheopsLogBuilder.ActionType;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.servlet.ServletContext;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.PATH_PARAM;
import static online.kheops.auth_server.user.AlbumUserPermissions.*;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_FORM_PARAMETER;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;


@Path("/")
public class AlbumResource {

    private static final Logger LOG = Logger.getLogger(AlbumResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @Context
    private ServletContext context;

    @POST
    @Secured
    @UserAccessSecured
    @Path("albums")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newAlbum(@FormParam("name") @NotBlank String name,
                             @DefaultValue("") @FormParam("description") String description,
                             @FormParam("addUser") Boolean addUser, @FormParam("downloadSeries") Boolean downloadSeries,
                             @FormParam("sendSeries") Boolean sendSeries, @FormParam("deleteSeries") Boolean deleteSeries,
                             @FormParam("addSeries") Boolean addSeries, @FormParam("writeComments") Boolean writeComments) {

        name = name.trim();
        description = description.trim();
        if(name.length() > DbColumnSize.ALBUM_NAME) {
            final ErrorResponse errorResponse = new ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'name' is too long max expected: " + DbColumnSize.ALBUM_NAME + " characters but got :" + name.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }
        if(description.length() > DbColumnSize.ALBUM_DESCRIPTION) {
            final ErrorResponse errorResponse = new ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'description' is too long max expected: " + DbColumnSize.ALBUM_DESCRIPTION + " characters but got :" + description.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        final UsersPermission usersPermission = new UsersPermission();

        if (addUser != null) { usersPermission.setAddUser(addUser); }
        if (downloadSeries != null) { usersPermission.setDownloadSeries(downloadSeries); }
        if (sendSeries != null) { usersPermission.setSendSeries(sendSeries); }
        if (deleteSeries != null) { usersPermission.setDeleteSeries(deleteSeries); }
        if (addSeries != null) { usersPermission.setAddSeries(addSeries); }
        if (writeComments != null) { usersPermission.setWriteComments(writeComments); }

        final AlbumResponse albumResponse = Albums.createAlbum(kheopsPrincipal.getUser(), name, description, usersPermission);
        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumResponse.getId())
                .action(ActionType.NEW_ALBUM)
                .userPermission(usersPermission)
                .log();
        return Response.status(CREATED).entity(albumResponse).build();
    }

    @GET
    @Secured
    @Path("albums")
    @UserAccessSecured
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getAlbums() throws BadQueryParametersException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final PairListXTotalCount<AlbumResponse> pairAlbumsTotalAlbum;

        final AlbumQueryParams albumQueryParams = new AlbumQueryParams(kheopsPrincipal, uriInfo.getQueryParameters());
        pairAlbumsTotalAlbum = Albums.getAlbumList(albumQueryParams);

        final GenericEntity<List<AlbumResponse>> genericAlbumResponsesList = new GenericEntity<List<AlbumResponse>>(pairAlbumsTotalAlbum.getAttributesList()) {};
        kheopsPrincipal.getKheopsLogBuilder()
                .action(ActionType.LIST_ALBUMS)
                .log();
        return Response.ok(genericAlbumResponsesList)
                .header(X_TOTAL_COUNT, pairAlbumsTotalAlbum.getXTotalCount())
                .build();
    }

    @GET
    @Secured
    @AlbumAccessSecured
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                             @QueryParam("includeUsers") @DefaultValue("false") boolean includeUsers)
            throws AlbumNotFoundException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal) securityContext.getUserPrincipal());
        final AlbumResponse albumResponse;

        if (!kheopsPrincipal.hasAlbumPermission(LIST_USERS, albumId) && includeUsers) {
            final ErrorResponse errorResponse = new ErrorResponseBuilder()
                    .message("'includeUser' forbidden")
                    .detail("You can not get the list of users with your token")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        albumResponse = Albums.getAlbum(kheopsPrincipal.getUser(), albumId, kheopsPrincipal.hasUserAccess(), includeUsers);

        final KheopsLogBuilder kheopsLog = kheopsPrincipal.getKheopsLogBuilder();
        if (includeUsers) {
            kheopsLog.action(ActionType.LIST_USERS);
        }
        kheopsLog.album(albumResponse.getId())
                .action(ActionType.GET_ALBUM)
                .log();
        return Response.status(OK).entity(albumResponse).build();
    }

    @PATCH
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = EDIT_ALBUM, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                              @FormParam("name") String name, @FormParam("description") String description,
                              @FormParam("addUser") Boolean addUser, @FormParam("downloadSeries") Boolean downloadSeries,
                              @FormParam("sendSeries") Boolean sendSeries, @FormParam("deleteSeries") Boolean deleteSeries,
                              @FormParam("addSeries") Boolean addSeries, @FormParam("writeComments") Boolean writeComments,
                              @FormParam("notificationNewSeries") Boolean notificationNewSeries,
                              @FormParam("notificationNewComment") Boolean notificationNewComment)
            throws AlbumNotFoundException, AlbumForbiddenException {


        if(name != null) {
            name = name .trim();
            if (name.length() > DbColumnSize.ALBUM_NAME) {
                final ErrorResponse errorResponse = new ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("Param 'name' is too long max expected: " + DbColumnSize.ALBUM_NAME + " characters but got :" + name.length())
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
            if (name.isEmpty()) {
                final ErrorResponse errorResponse = new ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("Param 'name' can not be empty max expected: " + DbColumnSize.ALBUM_NAME + " characters but got 0")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
        }
        if (description != null) {
            description = description.trim();
            if (description.length() > DbColumnSize.ALBUM_DESCRIPTION) {
                final ErrorResponse errorResponse = new ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("Param 'description' is too long max expected: " + DbColumnSize.ALBUM_DESCRIPTION + " characters but got :" + description.length())
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
            if (description.isEmpty()) {
                final ErrorResponse errorResponse = new ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("Param 'description' can either be empty or must contain non-space characters")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
        } else {
            description = "";
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        final AlbumResponse albumResponse;
        final UsersPermission usersPermission = new UsersPermission();

        usersPermission.setAddUser(addUser);
        usersPermission.setDownloadSeries(downloadSeries);
        usersPermission.setSendSeries(sendSeries);
        usersPermission.setDeleteSeries(deleteSeries);
        usersPermission.setAddSeries(addSeries);
        usersPermission.setWriteComments(writeComments);

        try {
            albumResponse = Albums.editAlbum(kheopsPrincipal.getUser(), albumId, name, description, usersPermission, notificationNewComment, notificationNewSeries);
        } catch (UserNotMemberException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumResponse.getId())
                .userPermission(usersPermission)
                .action(ActionType.EDIT_ALBUM)
                .log();
        return Response.status(OK).entity(albumResponse).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = DELETE_ALBUM, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId)
            throws AlbumNotFoundException, UserNotMemberException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        Albums.deleteAlbum(context, kheopsPrincipal.getUser(), albumId);

        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumId)
                .action(ActionType.DELETE_ALBUM)
                .log();

        return Response.status(NO_CONTENT).build();
    }

    @GET
    @Secured
    @AlbumAccessSecured
    @UserAccessSecured
    @AlbumPermissionSecured(permission = LIST_USERS, context = PATH_PARAM)
    @Path("albums/{album:"+AlbumId.ID_PATTERN+"}/users")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                  @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                  @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset)
            throws AlbumNotFoundException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final PairListXTotalCount<UserResponse> pair;

        pair = Albums.getUsers(albumId, limit, offset);

        final GenericEntity<List<UserResponse>> genericUsersAlbumResponsesList = new GenericEntity<List<UserResponse>>(pair.getAttributesList()) {};
        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumId)
                .action(ActionType.LIST_USERS)
                .log();
        return Response.status(OK).entity(genericUsersAlbumResponsesList).header(X_TOTAL_COUNT, pair.getXTotalCount()).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = ADD_USER, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}/users/{user}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addUser(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                            @SuppressWarnings("RSReferenceInspection") @PathParam("user") String user)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException, UserNotMemberException{

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final User targetUser;

        targetUser = Albums.addUser(kheopsPrincipal.getUser(), user, albumId, false, context);

        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumId)
                .action(ActionType.ADD_USER)
                .targetUser(targetUser.getSub())
                .log();
        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = ADD_ADMIN, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}/users/{user}/admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addAdmin(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                             @SuppressWarnings("RSReferenceInspection") @PathParam("user") String user)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotMemberException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final User targetUser;

        try {
            targetUser = Albums.addUser(kheopsPrincipal.getUser(), user, albumId, true, context);
        } catch (UserNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumId)
                .action(ActionType.ADD_ADMIN)
                .targetUser(targetUser.getSub())
                .log();
        return Response.status(NO_CONTENT).build();//todo NO_CONTENT => CREATED ????
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = REMOVE_ADMIN, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}/users/{user}/admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeAdmin(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                @SuppressWarnings("RSReferenceInspection") @PathParam("user") String user)
            throws AlbumNotFoundException, UserNotFoundException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final User targetUser;

        try {
            targetUser = Albums.removeAdmin(kheopsPrincipal.getUser(), user, albumId);
        } catch (UserNotMemberException e) {
            LOG.log(Level.INFO, String.format("Remove an admin userName:%s from the album id:%s by user :%s FAILED", user, albumId, kheopsPrincipal.getName()), e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumId)
                .action(ActionType.REMOVE_ADMIN)
                .targetUser(targetUser.getSub())
                .log();
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = REMOVE_USER, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}/users/{user}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteUser(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                               @SuppressWarnings("RSReferenceInspection") @PathParam("user") String user)
            throws AlbumNotFoundException, AlbumForbiddenException, UserNotFoundException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final User targetUser;

        try {
            targetUser = Albums.deleteUser(context, kheopsPrincipal.getUser(), user, albumId);
        } catch (UserNotMemberException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumId)
                .action(ActionType.REMOVE_USER)
                .targetUser(targetUser.getSub())
                .log();
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @AlbumAccessSecured
    @UserAccessSecured
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addFavorites(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId)
            throws AlbumNotFoundException {


        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        try {
            Albums.setFavorites(kheopsPrincipal.getUser(), albumId, true);
        } catch (UserNotMemberException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumId)
                .action(ActionType.ALBUM_ADD_FAVORITE)
                .log();
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteFavorites(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId)
            throws AlbumNotFoundException{

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        try {
            Albums.setFavorites(kheopsPrincipal.getUser(), albumId, false);
        } catch (UserNotMemberException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        kheopsPrincipal.getKheopsLogBuilder()
                .album(albumId)
                .action(ActionType.ALBUM_REMOVE_FAVORITE)
                .log();
        return Response.status(NO_CONTENT).build();
    }
}
