package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.series.Series;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.util.Consts.*;


@Path("/")
public class FavoriteResource {

    @Context
    private SecurityContext securityContext;


    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.EDIT_FAVORITES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addStudyToFavorites(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                        @QueryParam(ALBUM) String fromAlbumId,
                                        @QueryParam(INBOX) Boolean fromInbox) {

        return editStudyFavorites(studyInstanceUID, fromAlbumId, fromInbox, true, securityContext);
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.EDIT_FAVORITES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeStudyFromFavorites(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                             @QueryParam(ALBUM) String fromAlbumId,
                                             @QueryParam(INBOX) Boolean fromInbox) {

        return editStudyFavorites(studyInstanceUID, fromAlbumId, fromInbox, false, securityContext);
    }

    private Response editStudyFavorites(String studyInstanceUID, String fromAlbumId, Boolean fromInbox,
                                        boolean favorite, SecurityContext securityContext) {

        if ((fromInbox == null && fromAlbumId == null) || (fromInbox != null && fromAlbumId != null)) {
            return Response.status(BAD_REQUEST).entity("Use album XOR inbox query param").build();
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasStudyReadAccess(studyInstanceUID)) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            Studies.editFavorites(kheopsPrincipal.getUser(), studyInstanceUID, fromAlbumId, favorite);
        } catch (AlbumNotFoundException | StudyNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.EDIT_FAVORITES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addSeriesToFavorites(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                         @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID,
                                         @QueryParam(ALBUM) String fromAlbumId,
                                         @QueryParam(INBOX) Boolean fromInbox) {

        return editSeriesFavorites(studyInstanceUID, seriesInstanceUID, fromAlbumId, fromInbox, true, securityContext);
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.EDIT_FAVORITES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeSeriesFromFavorites(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                              @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID,
                                              @QueryParam(ALBUM) String fromAlbumId,
                                              @QueryParam(INBOX) Boolean fromInbox) {

        return editSeriesFavorites(studyInstanceUID, seriesInstanceUID, fromAlbumId, fromInbox, false, securityContext);
    }

    private Response editSeriesFavorites(String studyInstanceUID, String seriesInstanceUID, String fromAlbumId, Boolean fromInbox,
                                        boolean favorite, SecurityContext securityContext) {

        if ((fromInbox == null && fromAlbumId == null) || (fromInbox != null && fromAlbumId != null)) {
            return Response.status(BAD_REQUEST).entity("Use album XOR inbox query param").build();
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if (!kheopsPrincipal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }

        try {
            Series.editFavorites(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, fromAlbumId, favorite);
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }
}
