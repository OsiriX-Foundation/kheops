package online.kheops.auth_server.resource;


import online.kheops.auth_server.KheopsInstance;
import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.PairListXTotalCount;
import online.kheops.auth_server.webhook.*;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.PATH_PARAM;
import static online.kheops.auth_server.user.AlbumUserPermissions.MANAGE_WEBHOOK;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.*;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;
import static online.kheops.auth_server.webhook.Webhooks.*;

@Path("/")
public class WebhookResource {

    private static final Logger LOG = Logger.getLogger(WebhookResource.class.getName());

    @Context
    private SecurityContext securityContext;

    @Context
    private ServletContext context;

    @Inject
    KheopsInstance kheopsInstance;

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = MANAGE_WEBHOOK, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/webhooks")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newWebHook(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                               @FormParam("url") @NotNull String url,
                               @FormParam("name") @NotNull String name,
                               @FormParam("secret") String secret,
                               @FormParam("event") List<String> events,
                               @FormParam("enabled")@DefaultValue("true") boolean enabled)
            throws AlbumNotFoundException, BadQueryParametersException {

        final WebhookPostParameters webhookPostParameters = new WebhookPostParameters()
                .setAlbumId(albumId)
                .setEnabled(enabled)
                .setEvents(events)
                .setUrl(url)
                .setName(name)
                .setSecret(secret);


        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final WebhookResponse webhookResponse = createWebhook(webhookPostParameters, kheopsPrincipal.getUser(), kheopsPrincipal.getKheopsLogBuilder());

        return Response.status(CREATED).entity(webhookResponse).build();
    }

    @PATCH
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = MANAGE_WEBHOOK, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/webhooks/{webhook:"+ WebhookId.ID_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editWebHook(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                               @SuppressWarnings("RSReferenceInspection") @PathParam("webhook") String webhookId,
                               @FormParam("url") String url,
                               @FormParam("name") String name,
                               @FormParam("secret") String secret,
                               @FormParam("event") List<String> events,
                               @FormParam("add_event") List<String> addEvents,
                               @FormParam("remove_event") List<String> removeEvents,
                               @FormParam("enabled") Boolean enabled)

            throws AlbumNotFoundException, WebhookNotFoundException, BadQueryParametersException {

        final WebhookPatchParameters webhookPatchParameters = new WebhookPatchParameters.WebhookPatchParametersBuilder()
                .setWebhookId(webhookId)
                .setAlbumId(albumId)
                .setEnabled(enabled)
                .setName(name)
                .setSecret(secret)
                .setUrl(url)
                .setEvents(events)
                .setAddEvents(addEvents)
                .setRemoveEvents(removeEvents).build();

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final WebhookResponse webhookResponse = editWebhook(webhookPatchParameters, kheopsPrincipal.getUser(), kheopsPrincipal.getKheopsLogBuilder());

        return Response.status(OK).entity(webhookResponse).build();
    }

    @GET
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = MANAGE_WEBHOOK, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/webhooks/{webhook:"+ WebhookId.ID_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWebhook(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                               @SuppressWarnings("RSReferenceInspection") @PathParam("webhook") String webhookId,
                               @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                               @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset)
            throws AlbumNotFoundException, WebhookNotFoundException {


        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final WebhookResponse webhookResponse = Webhooks.getWebhook(webhookId, albumId, limit, offset, kheopsInstance, kheopsPrincipal.getKheopsLogBuilder());

        return Response.status(OK).entity(webhookResponse).build();
    }

    @GET
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = MANAGE_WEBHOOK, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/webhooks")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWebhooks(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                               @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                               @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset,
                               @QueryParam("url") String url )
            throws AlbumNotFoundException {


        final PairListXTotalCount<WebhookResponse> pairWebhooksTotalWebhook;

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        pairWebhooksTotalWebhook = Webhooks.getWebhooks(albumId, url, limit, offset);

        kheopsPrincipal.getKheopsLogBuilder()
                .action(KheopsLogBuilder.ActionType.LIST_WEBHOOK)
                .album(albumId)
                .log();

        final GenericEntity<List<WebhookResponse>> genericWebhookResponsesList = new GenericEntity<List<WebhookResponse>>(pairWebhooksTotalWebhook.getAttributesList()) {};
        return Response.status(OK).entity(genericWebhookResponsesList)
                .header(X_TOTAL_COUNT, pairWebhooksTotalWebhook.getXTotalCount())
                .build();
    }

    @DELETE
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = MANAGE_WEBHOOK, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/webhooks/{webhook:"+ WebhookId.ID_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeWebhook(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                  @SuppressWarnings("RSReferenceInspection") @PathParam("webhook") String webhookId)
            throws AlbumNotFoundException, WebhookNotFoundException {


        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        deleteWebhook(webhookId, albumId, kheopsPrincipal.getUser(), kheopsPrincipal.getKheopsLogBuilder());

        return Response.status(NO_CONTENT).build();
    }

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = MANAGE_WEBHOOK, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/webhooks/{webhook:"+ WebhookId.ID_PATTERN+"}/trigger")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response triggerWebhook(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                   @SuppressWarnings("RSReferenceInspection") @PathParam("webhook") String webhookId,
                                   @FormParam("event") String event,
                                   @FormParam(SeriesInstanceUID) List<String> seriesUID,
                                   @FormParam(StudyInstanceUID) @UIDValidator String studyUID,
                                   @FormParam("user") String user)
            throws AlbumNotFoundException, WebhookNotFoundException, UserNotMemberException, SeriesNotFoundException, StudyNotFoundException, UserNotFoundException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (event == null) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("'event' must be set")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        if ((user == null && studyUID == null) ||
                (user != null && studyUID != null)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Use only '"+StudyInstanceUID+"' xor 'user' not both")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        if(event.equalsIgnoreCase(WebhookType.NEW_SERIES.name())) {
            if (studyUID == null || seriesUID.isEmpty()) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("'" + StudyInstanceUID + "' and '" + StudyInstanceUID + "' must be set")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
            Webhooks.triggerNewSeriesWebhook(context, webhookId, albumId, studyUID, seriesUID, kheopsPrincipal.getUser());
        } else if (event.equalsIgnoreCase(WebhookType.NEW_USER.name())) {
            if (user == null) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_FORM_PARAMETER)
                        .detail("'user' must be set with 'event'='new_user'")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
            Webhooks.triggerNewUserWebhook(context, webhookId, albumId, user, kheopsPrincipal.getUser());
        } else {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("'event' is not valid")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        kheopsPrincipal.getKheopsLogBuilder()
                .action(KheopsLogBuilder.ActionType.TRIGGER_WEBHOOK)
                .album(albumId)
                .webhookID(webhookId)
                .log();

        return Response.status(ACCEPTED).build();
    }

}
