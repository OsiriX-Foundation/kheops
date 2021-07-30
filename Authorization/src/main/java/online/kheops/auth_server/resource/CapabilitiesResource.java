package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.*;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import online.kheops.auth_server.util.PairListXTotalCount;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.capability.Capabilities.generateCapability;
import static online.kheops.auth_server.capability.CapabilityId.ID_PATTERN;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.QUERY_PARAM;
import static online.kheops.auth_server.user.AlbumUserPermissions.MANAGE_CAPABILITIES_TOKEN;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_FORM_PARAMETER;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;


@Path("/")
public class CapabilitiesResource {
    private static final Logger LOG = Logger.getLogger(CapabilitiesResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @POST
    @Secured
    @CapabilitySecured
    @Path("capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewCapability(@NotNull @NotEmpty @FormParam("title") String title,
                                        @FormParam("expiration_time") String expirationTime,
                                        @FormParam("not_before_time") String notBeforeTime,
                                        @NotNull @FormParam("scope_type") String scopeType,
                                        @FormParam("album") String albumId,
                                        @DefaultValue("false") @FormParam("read_permission") boolean readPermission,
                                        @DefaultValue("false") @FormParam("appropriate_permission") boolean appropriatePermission,
                                        @DefaultValue("false") @FormParam("download_permission") boolean downloadPermission,
                                        @DefaultValue("false") @FormParam("write_permission") boolean writePermission)
            throws AlbumNotFoundException, UserNotFoundException {

        if(title.length() > DbColumnSize.CAPABILITY_DESCRIPTION) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_FORM_PARAMETER)
                    .detail("Param 'title' is too long max expected: " + DbColumnSize.CAPABILITY_DESCRIPTION + " characters but got :" + title.length())
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final KheopsPrincipal kheopsPrincipal = (KheopsPrincipal) securityContext.getUserPrincipal();
        final CapabilitiesResponse capabilityResponse;

        final CapabilityParametersBuilder capabilityParametersBuilder = new CapabilityParametersBuilder()
                .callingUser(kheopsPrincipal.getUser())
                .title(title)
                .readPermission(readPermission)
                .writePermission(writePermission);

        if (!readPermission) {
            if (appropriatePermission) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("'appropriatePermission' is availble only if 'readPermission' is True")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
            if (downloadPermission) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("'downloadPermission' is availble only if 'readPermission' is True")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
        }
        capabilityParametersBuilder.appropriatePermission(appropriatePermission)
                .downloadPermission(downloadPermission);
        if(notBeforeTime != null) {
            try {
            capabilityParametersBuilder.notBeforeTime(notBeforeTime);
            } catch (DateTimeParseException e) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("An error occured during parssing 'not_before_time'")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
        }
        if(expirationTime != null) {
            try {
                capabilityParametersBuilder.expirationTime(expirationTime);
            } catch (DateTimeParseException e) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("An error occured during parssing 'expiration_time'")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
        }

        try {
            capabilityParametersBuilder.scope(scopeType, albumId);
        } catch (BadQueryParametersException e) {
            return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
        } catch (IllegalArgumentException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("'scope_type' must be 'user' or 'album'")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final CapabilityParameters capabilityParameters = capabilityParametersBuilder.build();

        try {
            capabilityResponse = generateCapability(capabilityParameters, kheopsPrincipal.getKheopsLogBuilder());
        } catch (UserNotMemberException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        } catch (NewCapabilityForbidden e) {
            return Response.status(FORBIDDEN).entity(e.getErrorResponse()).build();
        } catch (BadQueryParametersException e) {
            return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
        }

        return Response.status(CREATED).entity(capabilityResponse).build();
    }

    @POST
    @Secured
    @CapabilitySecured
    @Path("capabilities/{capability_id:"+ID_PATTERN+"}/revoke")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeCapability(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_id") String capabilityId)
            throws CapabilityNotFoundException {

        final KheopsPrincipal kheopsPrincipal = (KheopsPrincipal)securityContext.getUserPrincipal();
        final CapabilitiesResponse capabilityResponse = Capabilities.revokeCapability(kheopsPrincipal.getUser(), capabilityId, kheopsPrincipal.getKheopsLogBuilder());

        return Response.status(OK).entity(capabilityResponse).build();
    }

    @GET
    @Secured
    @CapabilitySecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = MANAGE_CAPABILITIES_TOKEN, context = QUERY_PARAM)
    @Path("capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapabilities(@QueryParam("valid") boolean valid,
                                    @QueryParam(ALBUM) String albumId,
                                    @QueryParam(USER) String userEmailOrSub,
                                    @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                    @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset)
            throws UserNotFoundException {

        final PairListXTotalCount<CapabilitiesResponse> pair;
        final KheopsPrincipal kheopsPrincipal = (KheopsPrincipal)securityContext.getUserPrincipal();
        final KheopsLogBuilder kheopsLogBuilder = kheopsPrincipal.getKheopsLogBuilder();

        if(albumId != null) {
            pair = Capabilities.getCapabilities(albumId, userEmailOrSub, valid, limit, offset);
            kheopsLogBuilder.album(albumId)
            .scope("album");
        } else {
            pair = Capabilities.getCapabilities(kheopsPrincipal.getUser(), valid, limit, offset);
            kheopsLogBuilder.scope("user");
        }

        kheopsLogBuilder.action(ActionType.GET_CAPABILITIES)
                .log();
        GenericEntity<List<CapabilitiesResponse>> genericCapabilityResponsesList = new GenericEntity<>(pair.getAttributesList()) {};
        return Response.status(OK).entity(genericCapabilityResponsesList).header(X_TOTAL_COUNT, pair.getXTotalCount()).build();
    }

    @GET
    @Secured
    @UserAccessSecured
    @Path("capabilities/{capability_token_id:" + ID_PATTERN + "}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapability(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_token_id") String capabilityTokenID)
            throws CapabilityNotFoundException {

        final KheopsPrincipal kheopsPrincipal = (KheopsPrincipal)securityContext.getUserPrincipal();
        final CapabilitiesResponse capabilityResponses = Capabilities.getCapability(capabilityTokenID, kheopsPrincipal.getUser());

        kheopsPrincipal.getKheopsLogBuilder().action(ActionType.GET_CAPABILITY)
                .capabilityID(capabilityTokenID)
                .log();
        return Response.status(OK).entity(capabilityResponses).build();
    }
}
