package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.album.*;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UsersPermission;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public class AlbumRessource {

    private static final Logger LOG = Logger.getLogger(AlbumRessource.class.getName());

    @Context
    private UriInfo uriInfo;

    @POST
    @Secured
    @Path("album")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newAlbum(@DefaultValue("Album_name") @FormParam("name") String name, @DefaultValue("") @FormParam("description") String description,
                             @FormParam("addUser") Boolean addUser, @FormParam("downloadSeries") Boolean downloadSeries,
                             @FormParam("sendSeries") Boolean sendSeries, @FormParam("deleteSeries") Boolean deleteSeries,
                             @FormParam("addSeries") Boolean addSeries, @FormParam("writeComments") Boolean writeComments,
                             @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        final UsersPermission usersPermission = new UsersPermission();

        if (addUser != null) { usersPermission.setAddUser(addUser); }
        if (downloadSeries != null) { usersPermission.setDownloadSeries(downloadSeries); }
        if (sendSeries != null){ usersPermission.setSendSeries(sendSeries); }
        if (deleteSeries != null) { usersPermission.setDeleteSeries(deleteSeries); }
        if (addSeries != null) { usersPermission.setAddSeries(addSeries); }
        if (writeComments != null) { usersPermission.setWriteComments(writeComments); }

        AlbumResponses.AlbumResponse albumResponse;

        try {
            albumResponse = Albums.cerateAlbum(callingUserPk, name, description, usersPermission);
        } catch (UserNotFoundException e) {
            LOG.log(Level.INFO, "Creating new album by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (JOOQException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        LOG.info("New album pk:"+albumResponse.id+" created by user pk:"+callingUserPk);
        return Response.status(Response.Status.CREATED).entity(albumResponse).build();
    }

    @GET
    @Secured
    @Path("album")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getAlbums(@Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        PairAlbumsTotalAlbum pairAlbumsTotalAlbum;

        try {
            pairAlbumsTotalAlbum = Albums.getAlbumList(callingUserPk, uriInfo.getQueryParameters());
        } catch (UserNotFoundException e) {
            LOG.log(Level.INFO, "Get albums list by user pk:"+callingUserPk+" FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (JOOQException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (BadQueryParametersException e) {
            LOG.log(Level.INFO, e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        GenericEntity<List<AlbumResponses.AlbumResponse>> genericAlbumResponsesList = new GenericEntity<List<AlbumResponses.AlbumResponse>>(pairAlbumsTotalAlbum.getAlbumsResponsesList()) {};
        return Response.status(Response.Status.OK).entity(genericAlbumResponsesList).header("X-Total-Count", pairAlbumsTotalAlbum.getAlbumsTotalCount()).build();
    }

    @GET
    @Secured
    @Path("album/{album:[1-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlbum(@PathParam("album") Long albumPk,@Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        AlbumResponses.AlbumResponse albumResponse;

        try {
            albumResponse = Albums.getAlbum(callingUserPk, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Get album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (JOOQException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.OK).entity(albumResponse).build();
    }


    @PATCH
    @Secured
    @Path("album/{album:[1-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editAlbum(@PathParam("album") Long albumPk,
                              @FormParam("name") String name, @FormParam("description") String description,
                              @FormParam("addUser") Boolean addUser, @FormParam("downloadSeries") Boolean downloadSeries,
                              @FormParam("sendSeries") Boolean sendSeries, @FormParam("deleteSeries") Boolean deleteSeries,
                              @FormParam("addSeries") Boolean addSeries, @FormParam("writeComments") Boolean writeComments,@Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        AlbumResponses.AlbumResponse albumResponse;
        final UsersPermission usersPermission = new UsersPermission();

        usersPermission.setAddUser(addUser);
        usersPermission.setDownloadSeries(downloadSeries);
        usersPermission.setSendSeries(sendSeries);
        usersPermission.setDeleteSeries(deleteSeries);
        usersPermission.setAddSeries(addSeries);
        usersPermission.setWriteComments(writeComments);

        try {
            albumResponse = Albums.editAlbum(callingUserPk, albumPk, name, description, usersPermission);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Edit album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Edit album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (JOOQException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.OK).entity(albumResponse).build();
    }

    @DELETE
    @Secured
    @Path("album/{album:[1-9][0-9]*}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteAlbum(@PathParam("album") Long albumPk,@Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Albums.deleteAlbum(callingUserPk, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Delete album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Delete album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }
        LOG.info("Delete album pk:" +albumPk+  " by user pk:"+callingUserPk+ " SUCCESS");
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Secured
    @Path("album/{album:[1-9][0-9]*}/users")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersAlbum(@PathParam("album") Long albumPk,@Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        List<AlbumResponses.UserAlbumResponse> usersAlbumResponse;

        try {
            usersAlbumResponse = Albums.getUsers(callingUserPk, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Get users list for album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Get users list for album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        GenericEntity<List<AlbumResponses.UserAlbumResponse>> genericUsersAlbumResponsesList = new GenericEntity<List<AlbumResponses.UserAlbumResponse>>(usersAlbumResponse) {};
        return Response.status(Response.Status.OK).entity(genericUsersAlbumResponsesList).build();
    }

    @PUT
    @Secured
    @Path("album/{album:[1-9][0-9]*}/users/{user}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addUser(@PathParam("album") Long albumPk, @PathParam("user") String user,
                            @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Albums.addUser(callingUserPk, user, albumPk, false);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Add an user userName:"+user+" to the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Add an user userName:"+user+" to the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Secured
    @Path("album/{album:[1-9][0-9]*}/users/{user}/admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addAdmin(@PathParam("album") Long albumPk, @PathParam("user") String user,
                             @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Albums.addUser(callingUserPk, user, albumPk, true);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Add an admin userName:"+user+" to the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Add an admin userName:"+user+" to the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("album/{album:[1-9][0-9]*}/users/{user}/admin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeAdmin(@PathParam("album") Long albumPk, @PathParam("user") String user,
                                @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Albums.removeAdmin(callingUserPk, user, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Remove an admin userName:"+user+" from the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Remove an admin userName:"+user+" from the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("album/{album:[1-9][0-9]*}/users/{user}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteUser(@PathParam("album") Long albumPk, @PathParam("user") String user,
                               @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Albums.deleteUser(callingUserPk, user, albumPk);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Remove an user userName:"+user+" from the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            LOG.log(Level.INFO, "Remove an user userName:"+user+" from the album pk:" +albumPk+  " by user pk:"+callingUserPk+ " FORBIDDEN", e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Secured
    @Path("album/{album:[1-9][0-9]*}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addFavorites(@PathParam("album") Long albumPk, @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        try {
            Albums.setFavorites(callingUserPk, albumPk, true);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO,"Add an album pk:" +albumPk+ " to favorites by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("album/{album:[1-9][0-9]*}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteFavorites(@PathParam("album") Long albumPk, @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        try {
            Albums.setFavorites(callingUserPk, albumPk, false);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.INFO, "Remove an album pk:" +albumPk+ " from favorites by user pk:"+callingUserPk+ " FAILED", e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
