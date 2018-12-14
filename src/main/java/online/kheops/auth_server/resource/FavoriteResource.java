package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UIDValidator;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.series.Series;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.util.Consts.*;


@Path("/")
public class FavoriteResource {

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @PUT
    @Secured
    @UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addStudyToFavorites(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                        @QueryParam(ALBUM) Long fromAlbumPk,
                                        @QueryParam(INBOX) Boolean fromInbox) {

        return editStudyFavorites(studyInstanceUID, fromAlbumPk, fromInbox, true, securityContext);
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeStudyFromFavorites(@PathParam(StudyInstanceUID) @UIDValidator  String studyInstanceUID,
                                             @QueryParam(ALBUM) Long fromAlbumPk,
                                             @QueryParam(INBOX) Boolean fromInbox) {

        return editStudyFavorites(studyInstanceUID, fromAlbumPk, fromInbox, false, securityContext);
    }

    private Response editStudyFavorites(String studyInstanceUID, Long fromAlbumPk, Boolean fromInbox,
                                        boolean favorite, SecurityContext securityContext) {

        if ((fromInbox == null && fromAlbumPk == null) || (fromInbox != null && fromAlbumPk != null)) {
            return Response.status(BAD_REQUEST).entity("Use album XOR inbox query param").build();
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if (fromAlbumPk != null && !kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.EDIT_FAVORITES, fromAlbumPk)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(FORBIDDEN).entity(e.getMessage()).build();
        }

        try {
            if (!kheopsPrincipal.hasStudyReadAccess(studyInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (StudyNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }
        try {
            Studies.editFavorites(callingUserPk, studyInstanceUID, fromAlbumPk, favorite);
        } catch (UserNotFoundException | AlbumNotFoundException | StudyNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addSeriesToFavorites(@PathParam(StudyInstanceUID) @UIDValidator  String studyInstanceUID,
                                         @PathParam(SeriesInstanceUID) @UIDValidator  String seriesInstanceUID,
                                         @QueryParam(ALBUM) Long fromAlbumPk,
                                         @QueryParam(INBOX) Boolean fromInbox) {

        return editSeriesFavorites(studyInstanceUID, seriesInstanceUID, fromAlbumPk, fromInbox, true, securityContext);
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeSeriesFromFavorites(@PathParam(StudyInstanceUID) @UIDValidator  String studyInstanceUID,
                                              @PathParam(SeriesInstanceUID) @UIDValidator  String seriesInstanceUID,
                                              @QueryParam(ALBUM) Long fromAlbumPk,
                                              @QueryParam(INBOX) Boolean fromInbox) {

        return editSeriesFavorites(studyInstanceUID, seriesInstanceUID, fromAlbumPk, fromInbox, false, securityContext);
    }

    private Response editSeriesFavorites(String studyInstanceUID, String seriesInstanceUID, Long fromAlbumPk, Boolean fromInbox,
                                        boolean favorite, SecurityContext securityContext) {
        if ((fromInbox == null && fromAlbumPk == null) || (fromInbox != null && fromAlbumPk != null)) {
            return Response.status(BAD_REQUEST).entity("Use album XOR inbox query param").build();
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if (fromAlbumPk != null && !kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.EDIT_FAVORITES, fromAlbumPk)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(FORBIDDEN).entity(e.getMessage()).build();
        }

        try {
            if (!kheopsPrincipal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }

        try {
            Series.editFavorites(callingUserPk, studyInstanceUID, seriesInstanceUID, fromAlbumPk, favorite);
        } catch (UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }
}
