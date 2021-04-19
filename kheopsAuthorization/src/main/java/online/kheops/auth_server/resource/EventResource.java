package online.kheops.auth_server.resource;


import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.event.EventResponse;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.servlet.ServletContext;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.PATH_PARAM;
import static online.kheops.auth_server.user.AlbumUserPermissions.READ_COMMENT;
import static online.kheops.auth_server.user.AlbumUserPermissions.WRITE_COMMENT;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_FORM_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.STUDY_NOT_FOUND;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;

@Path("/")
public class EventResource {

    private static final Logger LOG = Logger.getLogger(EventResource.class.getName());

    @Context
    private ServletContext context;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    @GET
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = READ_COMMENT, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/events")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvents(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                              @QueryParam("types") final List<String> types,
                              @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                              @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset)
            throws AlbumNotFoundException, BadQueryParametersException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM && types.contains("mutations")) {
            types.remove("mutations");
            types.add("comments");
        }

        final PairListXTotalCount<EventResponse> pair;

        KheopsLogBuilder kheopsLogBuilder = kheopsPrincipal.getKheopsLogBuilder();

        if (types.contains("comments") && types.contains("mutations")) {
            pair = Events.getEventsAlbum(kheopsPrincipal.getUser(), albumId, offset, limit);
            kheopsLogBuilder.events("comments_mutations");
        } else if (types.contains("comments")) {
            pair = Events.getCommentsAlbum(kheopsPrincipal.getUser(), albumId, offset, limit);
            kheopsLogBuilder.events("comments");
        } else if (types.contains("mutations")) {
            pair = Events.getMutationsAlbum(albumId, uriInfo.getQueryParameters(), offset, limit);
            kheopsLogBuilder.events("mutations");
        } else {
            pair = Events.getEventsAlbum(kheopsPrincipal.getUser(), albumId, offset, limit);
            kheopsLogBuilder.events("comments_mutations");
        }

        final GenericEntity<List<EventResponse>> genericEventsResponsesList = new GenericEntity<List<EventResponse>>(pair.getAttributesList()) {};
        kheopsLogBuilder.album(albumId)
                .action(KheopsLogBuilder.ActionType.LIST_EVENTS)
                .log();
        return Response.ok(genericEventsResponsesList)
                .header(X_TOTAL_COUNT, pair.getXTotalCount())
                .build();
    }

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = WRITE_COMMENT, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}/comments")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAlbumComment(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                     @FormParam("to_user") String user,
                                     @FormParam("comment") @NotNull @NotEmpty String comment)
            throws AlbumNotFoundException, UserNotFoundException {

        if(comment.length() > DB_COLUMN_SIZE.COMMENT) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'comment' is too long max expected: " + DB_COLUMN_SIZE.COMMENT + " characters but got :" + comment.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        Events.albumPostComment(kheopsPrincipal.getUser(), albumId, comment, user);

        kheopsPrincipal.getKheopsLogBuilder().album(albumId)
                .action(KheopsLogBuilder.ActionType.POST_COMMENT)
                .log();
        return Response.status(NO_CONTENT).build();
    }

    @GET
    @Secured
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/comments")
    public Response getComments(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasStudyViewAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("The study does not exist or you don't have access")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        final PairListXTotalCount<EventResponse> pair;
        pair = Events.getCommentsByStudyUID(kheopsPrincipal, studyInstanceUID, offset, limit);

        final GenericEntity<List<EventResponse>> genericEventsResponsesList = new GenericEntity<List<EventResponse>>(pair.getAttributesList()) {};
        kheopsPrincipal.getKheopsLogBuilder().study(studyInstanceUID)
                .action(KheopsLogBuilder.ActionType.LIST_EVENTS)
                .log();
        return Response.ok(genericEventsResponsesList)
                .header(X_TOTAL_COUNT, pair.getXTotalCount())
                .build();
    }

    @POST
    @Secured
    @UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/comments")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postStudiesComment(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                       @FormParam("to_user") String user,
                                       @FormParam("comment") @NotNull @NotEmpty String comment)
            throws StudyNotFoundException, BadQueryParametersException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if(!kheopsPrincipal.hasStudyViewAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("The study does not exist or you don't have access")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        if(comment.length() > DB_COLUMN_SIZE.COMMENT) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'comment' is too long max expected: " + DB_COLUMN_SIZE.COMMENT + " characters but got :" + comment.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        try {
            Events.studyPostComment(kheopsPrincipal.getUser(), studyInstanceUID, comment, user);
        } catch (UserNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        kheopsPrincipal.getKheopsLogBuilder().study(studyInstanceUID)
                .action(KheopsLogBuilder.ActionType.POST_COMMENT)
                .log();
        return Response.status(NO_CONTENT).build();
    }
}
