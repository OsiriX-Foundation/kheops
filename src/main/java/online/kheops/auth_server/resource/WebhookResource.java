package online.kheops.auth_server.resource;


import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.AlbumAccessSecured;
import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.webhook.WebhookId;
import online.kheops.auth_server.webhook.WebhookNotFoundException;
import online.kheops.auth_server.webhook.WebhookResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.PATH_PARAM;
import static online.kheops.auth_server.user.AlbumUserPermissions.MANAGE_WEBHOOK;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_FORM_PARAMETER;
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


}
