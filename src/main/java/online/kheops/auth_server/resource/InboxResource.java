package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.CapabilitySecured;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.capability.*;
import online.kheops.auth_server.capability.CapabilitiesResponses.CapabilityResponse;
import online.kheops.auth_server.series.Series;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.Studies;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.Consts;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.format.DateTimeParseException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.capability.Capabilities.generateCapability;
import static online.kheops.auth_server.series.Series.checkValidUID;


@Path("/")
public class InboxResource {

    @Context
    private UriInfo uriInfo;


    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addStudyToFavorites(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                        @QueryParam("album") Long fromAlbumPk,
                                        @QueryParam("inbox") Boolean fromInbox,
                                        @Context SecurityContext securityContext) {

        if ((fromInbox == null && fromAlbumPk == null) || (fromInbox != null && fromAlbumPk != null)) {
            return Response.status(BAD_REQUEST).entity("Use album XOR inbox query param").build();
        }

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        if (!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if (!kheopsPrincipal.hasStudyReadAccess(studyInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (StudyNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }

        try {
            Studies.editFavorites(callingUserPk, studyInstanceUID, fromAlbumPk, true);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }


        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeStudyFromFavorites(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                             @QueryParam("album") Long fromAlbumPk,
                                             @QueryParam("inbox") Boolean fromInbox,
                                             @Context SecurityContext securityContext) {

        if ((fromInbox == null && fromAlbumPk == null) || (fromInbox != null && fromAlbumPk != null)) {
            return Response.status(BAD_REQUEST).entity("Use album XOR inbox query param").build();
        }

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        if (!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if (!kheopsPrincipal.hasStudyReadAccess(studyInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (StudyNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }
        try {
            Studies.editFavorites(callingUserPk, studyInstanceUID, fromAlbumPk, false);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(NO_CONTENT).build();
    }


    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addSeriesToFavorites(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                         @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                         @QueryParam("album") Long fromAlbumPk,
                                         @QueryParam("inbox") Boolean fromInbox,
                                         @Context SecurityContext securityContext) {

        if ((fromInbox == null && fromAlbumPk == null) || (fromInbox != null && fromAlbumPk != null)) {
            return Response.status(BAD_REQUEST).entity("Use album XOR inbox query param").build();
        }

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        if (!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if (!kheopsPrincipal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }

        try {
            Series.editFavorites(callingUserPk, studyInstanceUID, seriesInstanceUID, fromAlbumPk, true);
        } catch (UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/favorites")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response removeSeriesFromFavorites(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                              @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                              @QueryParam("album") Long fromAlbumPk,
                                              @QueryParam("inbox") Boolean fromInbox,
                                              @Context SecurityContext securityContext) {

        if ((fromInbox == null && fromAlbumPk == null) || (fromInbox != null && fromAlbumPk != null)) {
            return Response.status(BAD_REQUEST).entity("Use album XOR inbox query param").build();
        }

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        if (!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if (!kheopsPrincipal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }

        try {
            Series.editFavorites(callingUserPk, studyInstanceUID, seriesInstanceUID, fromAlbumPk, false);
        } catch (UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }
}
