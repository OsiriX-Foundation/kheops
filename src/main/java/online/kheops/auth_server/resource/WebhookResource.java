package online.kheops.auth_server.resource;


import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.AlbumResponse;
import online.kheops.auth_server.annotation.AlbumAccessSecured;
import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.PairListXTotalCount;
import online.kheops.auth_server.webhook.WebhookId;
import online.kheops.auth_server.webhook.WebhookNotFoundException;
import online.kheops.auth_server.webhook.WebhookResponse;
import online.kheops.auth_server.webhook.Webhooks;

import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.PATH_PARAM;
import static online.kheops.auth_server.user.AlbumUserPermissions.MANAGE_WEBHOOK;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_FORM_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;
import static online.kheops.auth_server.webhook.Webhooks.*;

@Path("/")
public class WebhookResource {

    private static final Logger LOG = Logger.getLogger(WebhookResource.class.getName());

    @Context
    private SecurityContext securityContext;

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = MANAGE_WEBHOOK, context = PATH_PARAM)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/webhooks")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newWebHook(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                               @FormParam("url") String url,
                               @FormParam("name") String name,
                               @FormParam("secret") String secret,
                               @FormParam("new_series")@DefaultValue("false") boolean newSeries,
                               @FormParam("new_user")@DefaultValue("false") boolean newUser)
            throws AlbumNotFoundException {

        name = name.trim();
        if(name.length() > Consts.DB_COLUMN_SIZE.WEBHOOK_NAME) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'name' is too long max expected: " + Consts.DB_COLUMN_SIZE.WEBHOOK_NAME + " characters but got :" + name.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        if(url.length() > Consts.DB_COLUMN_SIZE.WEBHOOK_URL) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'url' is too long max expected: " + Consts.DB_COLUMN_SIZE.WEBHOOK_URL + " characters but got :" + url.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        final WebhookResponse webhookResponse = createWebhook(url, albumId, kheopsPrincipal.getUser(), name, secret, newSeries, newUser, kheopsPrincipal.getKheopsLogBuilder());

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
    public Response newWebHook(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                               @SuppressWarnings("RSReferenceInspection") @PathParam("webhook") String webhookId,
                               @FormParam("url") String url,
                               @FormParam("name") String name,
                               @FormParam("secret") String secret,
                               @FormParam("new_series") Boolean newSeries,
                               @FormParam("new_user") Boolean newUser)
            throws AlbumNotFoundException, WebhookNotFoundException {

        name = name.trim();
        if(name.length() > Consts.DB_COLUMN_SIZE.WEBHOOK_NAME) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'name' is too long max expected: " + Consts.DB_COLUMN_SIZE.WEBHOOK_NAME + " characters but got :" + name.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        if(url != null && url.length() > Consts.DB_COLUMN_SIZE.WEBHOOK_URL) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'url' is too long max expected: " + Consts.DB_COLUMN_SIZE.WEBHOOK_URL + " characters but got :" + url.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        final WebhookResponse webhookResponse = editWebhook(webhookId, url, albumId, kheopsPrincipal.getUser(), name, secret, newSeries, newUser, kheopsPrincipal.getKheopsLogBuilder());

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
                               @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer historyLimit,
                               @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer historyOffset)
            throws AlbumNotFoundException, WebhookNotFoundException {


        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final WebhookResponse webhookResponse = Webhooks.getWebhook(webhookId, albumId, historyLimit, historyOffset, kheopsPrincipal.getKheopsLogBuilder());

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
                               @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset)
            throws AlbumNotFoundException, WebhookNotFoundException {


        final PairListXTotalCount<WebhookResponse> pairWebhooksTotalWebhook;

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        pairWebhooksTotalWebhook = Webhooks.getWebhooks(albumId, limit, offset);

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
                                   @FormParam(SeriesInstanceUID) String seriesUID,
                                   @FormParam("user") String user)
            throws AlbumNotFoundException, WebhookNotFoundException {


        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if ((user == null && seriesUID == null) ||
                (user != null && seriesUID != null)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Use only '"+SeriesInstanceUID+"' xor 'user' not both")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        Webhooks.triggerWebhook(webhookId, albumId, user, seriesUID, kheopsPrincipal.getUser());

        kheopsPrincipal.getKheopsLogBuilder()
                .action(KheopsLogBuilder.ActionType.TRIGGER_WEBHOOK)
                .album(albumId)
                .webhookID(webhookId)
                .log();

        return Response.status(NO_CONTENT).build();
    }

}
