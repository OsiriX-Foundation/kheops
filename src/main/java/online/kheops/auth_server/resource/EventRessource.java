package online.kheops.auth_server.resource;


import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.AlbumForbiddenException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.event.EventResponses;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.series.Series.checkValidUID;

@Path("/")
public class EventRessource {

    private static final Logger LOG = Logger.getLogger(EventRessource.class.getName());

    @Context
    ServletContext context;

    @GET
    @Secured
    @Path("album/{album:[1-9][0-9]*}/events")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvents(@PathParam("album") Long albumPk,
                              @QueryParam("types") final List<String> types, @QueryParam(Consts.QUERY_PARAMETER_LIMIT) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                              @QueryParam(Consts.QUERY_PARAMETER_OFFSET) @DefaultValue("0") Integer offset, @Context SecurityContext securityContext) {

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        try {
            if(!kheopsPrincipal.hasAlbumAccess(albumPk)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
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
                pair = Events.getEventsAlbum(callingUserPk, albumPk, offset, limit);
            } else if (types.contains("comments")) {
                pair = Events.getCommentsAlbum(callingUserPk, albumPk, offset, limit);
            } else if (types.contains("mutations")) {
                pair = Events.getMutationsAlbum(callingUserPk, albumPk, offset, limit);
            } else {
                pair = Events.getEventsAlbum(callingUserPk, albumPk, offset, limit);
            }
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final GenericEntity<List<EventResponses.EventResponse>> genericEventsResponsesList = new GenericEntity<List<EventResponses.EventResponse>>(pair.getAttributesList()) {};
        return Response.status(OK).entity(genericEventsResponsesList).header("X-Total-Count",pair.getXTotalCount()).build();
    }

    @POST
    @Secured
    @Path("album/{album:[1-9][0-9]*}/comments")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAlbumComment(@PathParam("album") Long albumPk,
                                @FormParam("to_user") String user, @FormParam("comment") String comment,
                                @Context SecurityContext securityContext) {

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if(!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.WRITE_COMMENT, albumPk)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Events.albumPostComment(callingUserPk, albumPk, comment, user);
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
    public Response getComments(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                @QueryParam(Consts.QUERY_PARAMETER_LIMIT) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                @QueryParam(Consts.QUERY_PARAMETER_OFFSET) @DefaultValue("0") Integer offset,
                                @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

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
        return Response.status(OK).entity(genericEventsResponsesList).header("X-Total-Count", pair.getXTotalCount()).build();
    }

    @POST
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/comments")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postStudiesComment(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                @FormParam("to_user") String user, @FormParam("comment") String comment,
                                @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if(!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

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
