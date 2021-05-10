package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.series.Series;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.QUERY_PARAM;
import static online.kheops.auth_server.user.AlbumUserPermissions.EDIT_FAVORITES;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.*;


@Path("/")
public class FavoriteResource {

    @Context
    private SecurityContext securityContext;


    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = EDIT_FAVORITES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addStudyToFavorites(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                        @QueryParam(ALBUM) String fromAlbumId,
                                        @QueryParam(INBOX) Boolean fromInbox)
            throws AlbumNotFoundException, StudyNotFoundException {

        return editStudyFavorites(studyInstanceUID, fromAlbumId, fromInbox, true, securityContext);
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = EDIT_FAVORITES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeStudyFromFavorites(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                             @QueryParam(ALBUM) String fromAlbumId,
                                             @QueryParam(INBOX) Boolean fromInbox)
            throws AlbumNotFoundException, StudyNotFoundException {

        return editStudyFavorites(studyInstanceUID, fromAlbumId, fromInbox, false, securityContext);
    }

    private Response editStudyFavorites(String studyInstanceUID, String fromAlbumId, Boolean fromInbox,
                                        boolean favorite, SecurityContext securityContext)
            throws AlbumNotFoundException, StudyNotFoundException {

        if ((fromInbox == null && fromAlbumId == null) || (fromInbox != null && fromAlbumId != null)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Use album XOR inbox query param")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasStudyViewAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("The study does not exist or you don't have access")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        Studies.editFavorites(kheopsPrincipal.getUser(), studyInstanceUID, fromAlbumId, favorite, kheopsPrincipal.getKheopsLogBuilder());
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = EDIT_FAVORITES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addSeriesToFavorites(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                         @PathParam(SERIES_INSTANCE_UID) @UIDValidator String seriesInstanceUID,
                                         @QueryParam(ALBUM) String fromAlbumId,
                                         @QueryParam(INBOX) Boolean fromInbox)
            throws AlbumNotFoundException, SeriesNotFoundException {

        return editSeriesFavorites(studyInstanceUID, seriesInstanceUID, fromAlbumId, fromInbox, true, securityContext);
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = EDIT_FAVORITES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeSeriesFromFavorites(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                              @PathParam(SERIES_INSTANCE_UID) @UIDValidator String seriesInstanceUID,
                                              @QueryParam(ALBUM) String fromAlbumId,
                                              @QueryParam(INBOX) Boolean fromInbox)
            throws AlbumNotFoundException, SeriesNotFoundException {

        return editSeriesFavorites(studyInstanceUID, seriesInstanceUID, fromAlbumId, fromInbox, false, securityContext);
    }

    private Response editSeriesFavorites(String studyInstanceUID, String seriesInstanceUID, String fromAlbumId, Boolean fromInbox,
                                        boolean favorite, SecurityContext securityContext)
            throws AlbumNotFoundException, SeriesNotFoundException {

        if ((fromInbox == null && fromAlbumId == null) || (fromInbox != null && fromAlbumId != null)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Use album XOR inbox query param")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasSeriesViewAccess(studyInstanceUID, seriesInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist or you don't have access")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        Series.editFavorites(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, fromAlbumId, favorite, kheopsPrincipal.getKheopsLogBuilder());

        return Response.status(NO_CONTENT).build();
    }
}
