package online.kheops.auth_server.resource;


import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.event.EventResponse;
import online.kheops.auth_server.event.Events;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;
import online.kheops.auth_server.util.PairListXTotalCount;
import org.hibernate.validator.constraints.NotEmpty;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;

@Path("/")
public class EventResource {

    private static final Logger LOG = Logger.getLogger(EventResource.class.getName());

    @Context
    ServletContext context;

    @Context
    private SecurityContext securityContext;

    @GET
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.READ_COMMENT)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/events")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvents(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                              @QueryParam("types") final List<String> types,
                              @QueryParam(QUERY_PARAMETER_LIMIT) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                              @QueryParam(QUERY_PARAMETER_OFFSET) @DefaultValue("0") Integer offset) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM && types.contains("mutation")) {
            types.remove("mutation");
            types.add("comments");
        }

        final PairListXTotalCount<EventResponse> pair;

        if( offset < 0 ) {
            return Response.status(BAD_REQUEST).entity("offset must be >= 0").build();
        }
        if( limit < 0 ) {
            return Response.status(BAD_REQUEST).entity("limit must be >= 0").build();
        }

        try {
            if (types.contains("comments") && types.contains("mutations")) {
                pair = Events.getEventsAlbum(kheopsPrincipal.getUser(), albumId, offset, limit);
            } else if (types.contains("comments")) {
                pair = Events.getCommentsAlbum(kheopsPrincipal.getUser(), albumId, offset, limit);
            } else if (types.contains("mutations")) {
                pair = Events.getMutationsAlbum(albumId, offset, limit);
            } else {
                pair = Events.getEventsAlbum(kheopsPrincipal.getUser(), albumId, offset, limit);
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final GenericEntity<List<EventResponse>> genericEventsResponsesList = new GenericEntity<List<EventResponse>>(pair.getAttributesList()) {};
        return Response.ok(genericEventsResponsesList)
                .header(X_TOTAL_COUNT, pair.getXTotalCount())
                .build();
    }

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.WRITE_COMMENT)
    @Path("albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}/comments")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAlbumComment(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                     @FormParam("to_user") String user,
                                     @FormParam("comment") @NotNull @NotEmpty String comment) {

        if(comment.length() > DB_COLUMN_SIZE.COMMENT) {
            return Response.status(BAD_REQUEST).entity("Param 'comment' is too long. max expected: " + DB_COLUMN_SIZE.COMMENT + " characters but got :" + comment.length()).build();
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            Events.albumPostComment(kheopsPrincipal.getUser(), albumId, comment, user);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            LOG.log(Level.WARNING, "Not found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadQueryParametersException e) {
            LOG.log(Level.WARNING, "Bad request", e);
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

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasStudyReadAccess(studyInstanceUID)) {
            return Response.status(FORBIDDEN).entity("You don't have access to the Study:" + studyInstanceUID + " or it does not exist").build();
        }

        final PairListXTotalCount<EventResponse> pair;

        if( offset < 0 ) {
            return Response.status(BAD_REQUEST).entity("offset must be >= 0").build();
        }
        if( limit < 0 ) {
            return Response.status(BAD_REQUEST).entity("limit must be >= 0").build();
        }

        pair = Events.getCommentsByStudyUID(kheopsPrincipal.getUser(), studyInstanceUID, offset, limit);


        final GenericEntity<List<EventResponse>> genericEventsResponsesList = new GenericEntity<List<EventResponse>>(pair.getAttributesList()) {};
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
                                       @FormParam("comment") @NotNull @NotEmpty String comment) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if(!kheopsPrincipal.hasStudyReadAccess(studyInstanceUID)) {
            return Response.status(FORBIDDEN).entity("You don't have access to the Study:"+studyInstanceUID+" or it does not exist").build();
        }

        if(comment.length() > DB_COLUMN_SIZE.COMMENT) {
            return Response.status(BAD_REQUEST).entity("Param 'comment' is too long. max expected: " + DB_COLUMN_SIZE.COMMENT + " characters but got :" + comment.length()).build();
        }

        try {
            Events.studyPostComment(kheopsPrincipal.getUser(), studyInstanceUID, comment, user);
        } catch (UserNotFoundException | StudyNotFoundException e) {
            LOG.log(Level.WARNING, "Not Found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadQueryParametersException e) {
            LOG.log(Level.WARNING, "Bad Request", e);
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }
}
