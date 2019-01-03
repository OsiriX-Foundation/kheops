package online.kheops.auth_server.resource;


import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.Albums;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.event.EventResponses;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.util.Consts.QUERY_PARAMETER_LIMIT;
import static online.kheops.auth_server.util.Consts.QUERY_PARAMETER_OFFSET;
import static online.kheops.auth_server.util.Consts.StudyInstanceUID;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;

@Path("/")
public class EventRessource {

    private static final Logger LOG = Logger.getLogger(EventRessource.class.getName());

    @Context
    ServletContext context;

    @Context
    private SecurityContext securityContext;

    @GET
    @Secured
    @AlbumAccessSecured
    @Path("album/{album:"+Albums.ID_PATTERN+"}/events")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvents(@SuppressWarnings("RSReferenceInspection") @PathParam("album") String albumId,
                              @QueryParam("types") final List<String> types,
                              @QueryParam(QUERY_PARAMETER_LIMIT) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                              @QueryParam(QUERY_PARAMETER_OFFSET) @DefaultValue("0") Integer offset) {

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        //TODO check if permission read comments
        //TODO if album token : only comments not mutations

        if (kheopsPrincipal.getScope() != ScopeType.USER && types.contains("mutation")) {
            return Response.status(FORBIDDEN).build();
        }

        final long callingUserPk = kheopsPrincipal.getDBID();
        final PairListXTotalCount<EventResponses.EventResponse> pair;

        if( offset < 0 || limit < 0 ) {
            return Response.status(BAD_REQUEST).build();
        }

        try {
            if (types.contains("comments") && types.contains("mutations")) {
                pair = Events.getEventsAlbum(callingUserPk, albumId, offset, limit);
            } else if (types.contains("comments")) {
                pair = Events.getCommentsAlbum(callingUserPk, albumId, offset, limit);
            } else if (types.contains("mutations")) {
                pair = Events.getMutationsAlbum(albumId, offset, limit);
            } else {
                pair = Events.getEventsAlbum(callingUserPk, albumId, offset, limit);
            }
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final GenericEntity<List<EventResponses.EventResponse>> genericEventsResponsesList = new GenericEntity<List<EventResponses.EventResponse>>(pair.getAttributesList()) {};
        return Response.ok(genericEventsResponsesList)
                .header(X_TOTAL_COUNT, pair.getXTotalCount())
                .build();
    }

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.WRITE_COMMENT)
    @Path("album/{album:"+Albums.ID_PATTERN+"}/comments")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAlbumComment(@SuppressWarnings("RSReferenceInspection") @PathParam("album") String albumId,
                                     @FormParam("to_user") String user,
                                     @FormParam("comment") String comment) {

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Events.albumPostComment(callingUserPk, albumId, comment, user);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadQueryParametersException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    @GET
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/comments")
    public Response getComments(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                @QueryParam(QUERY_PARAMETER_LIMIT) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                @QueryParam(QUERY_PARAMETER_OFFSET) @DefaultValue("0") Integer offset) {

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if (!kheopsPrincipal.hasStudyReadAccess(studyInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (StudyNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final long callingUserPk = kheopsPrincipal.getDBID();
        final PairListXTotalCount<EventResponses.EventResponse> pair;

        if( offset < 0 || limit < 0 ) {
            return Response.status(BAD_REQUEST).build();
        }

        try {
            pair = Events.getCommentsByStudyUID(callingUserPk, studyInstanceUID, offset, limit);
        } catch(UserNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final GenericEntity<List<EventResponses.EventResponse>> genericEventsResponsesList = new GenericEntity<List<EventResponses.EventResponse>>(pair.getAttributesList()) {};
        return Response.ok(genericEventsResponsesList)
                .header(X_TOTAL_COUNT, pair.getXTotalCount())
                .build();
    }

    @POST
    @Secured
    @UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/comments")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postStudiesComment(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                       @FormParam("to_user") String user,
                                       @FormParam("comment") String comment) {

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Events.studyPostComment(callingUserPk, studyInstanceUID, comment, user);
        } catch (UserNotFoundException | StudyNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadQueryParametersException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }
}
