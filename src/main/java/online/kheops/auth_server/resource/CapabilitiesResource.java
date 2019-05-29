package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.*;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.PairListXTotalCount;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.capability.Capabilities.generateCapability;
import static online.kheops.auth_server.capability.CapabilityId.ID_PATTERN;
import static online.kheops.auth_server.capability.CapabilityToken.TOKEN_PATTERN;
import static online.kheops.auth_server.util.Consts.*;
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
    @FormURLEncodedContentType
    @Path("capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewCapability(@NotNull @NotEmpty @FormParam("title") String title,
                                        @FormParam("expiration_time") String expirationTime,
                                        @FormParam("not_before_time") String notBeforeTime,
                                        @NotNull @FormParam("scope_type") String scopeType,
                                        @FormParam("album") String albumId,
                                        @NotNull @FormParam("read_permission") boolean readPermission,
                                        @NotNull @FormParam("appropriate_permission") boolean appropriatePermission,
                                        @NotNull @FormParam("download_permission") boolean downloadPermission,
                                        @NotNull @FormParam("write_permission") boolean writePermission) {

        if(title.length() > Consts.DB_COLUMN_SIZE.CAPABILITY_DESCRIPTION) {
            return Response.status(BAD_REQUEST).entity("Param 'title' is too long. max expected: " + Consts.DB_COLUMN_SIZE.CAPABILITY_DESCRIPTION + " characters but got :" + title.length()).build();
        }

        final KheopsPrincipalInterface kheopsPrincipal = (KheopsPrincipalInterface) securityContext.getUserPrincipal();
        final CapabilitiesResponse capabilityResponse;

        final CapabilityParametersBuilder capabilityParametersBuilder = new CapabilityParametersBuilder()
                .callingUser(kheopsPrincipal.getUser())
                .title(title)
                .readPermission(readPermission)
                .writePermission(writePermission);

        if (readPermission) {
            capabilityParametersBuilder.appropriatePermission(appropriatePermission)
                    .downloadPermission(downloadPermission);
        }
        if(notBeforeTime != null) {
            try {
            capabilityParametersBuilder.notBeforeTime(notBeforeTime);
            } catch (DateTimeParseException e) {
                return Response.status(BAD_REQUEST).entity("Bad query parameter {not_before_time}").build();
            }
        }
        if(expirationTime != null) {
            try {
                capabilityParametersBuilder.expirationTime(expirationTime);

            } catch (DateTimeParseException e) {
                return Response.status(BAD_REQUEST).entity("Bad query parameter {expiration_time}").build();
            }
        }

        try {
            capabilityParametersBuilder.scope(scopeType, albumId);
        } catch (CapabilityBadRequestException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(BAD_REQUEST).entity("{scope_type} = user or album. Not : "+scopeType).build();
        }

        final CapabilityParameters capabilityParameters = capabilityParametersBuilder.build();

        try {
            capabilityResponse = generateCapability(capabilityParameters);
        } catch (UserNotFoundException | AlbumNotFoundException | UserNotMemberException e) {
            LOG.log(Level.WARNING, "Bad Request", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (NewCapabilityForbidden e) {
            LOG.log(Level.WARNING, "Forbidden", e);
            return Response.status(FORBIDDEN).entity(e.getMessage()).build();
        } catch (CapabilityBadRequestException e) {
            LOG.log(Level.WARNING, "Bad Request", e);
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.status(CREATED).entity(capabilityResponse).build();
    }

    @POST
    @Secured
    @CapabilitySecured
    @Path("capabilities/{capability_id:"+ID_PATTERN+"}/revoke")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeCapability(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_id") String capabilityId) {

        final KheopsPrincipalInterface kheopsPrincipal = (KheopsPrincipalInterface)securityContext.getUserPrincipal();
        final CapabilitiesResponse capabilityResponse;

        try {
            capabilityResponse = Capabilities.revokeCapability(kheopsPrincipal.getUser(), capabilityId);
        } catch (CapabilityNotFoundException e) {
            LOG.log(Level.WARNING, "Not Found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(OK).entity(capabilityResponse).build();
    }

    @GET
    @Secured
    @CapabilitySecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.MANAGE_CAPABILITIES_TOKEN)
    @Path("capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapabilities(@QueryParam("valid") boolean valid,
                                    @QueryParam(ALBUM) String albumId,
                                    @QueryParam(QUERY_PARAMETER_LIMIT) @Min(0) @DefaultValue(""+Integer.MAX_VALUE) Integer limit,
                                    @QueryParam(QUERY_PARAMETER_OFFSET) @Min(0) @DefaultValue("0") Integer offset) {

        final PairListXTotalCount<CapabilitiesResponse> pair;

        if(albumId != null) {
            pair = Capabilities.getCapabilities(albumId, valid, limit, offset);
        } else {
            final KheopsPrincipalInterface kheopsPrincipal = (KheopsPrincipalInterface)securityContext.getUserPrincipal();
            pair = Capabilities.getCapabilities(kheopsPrincipal.getUser(), valid, limit, offset);
        }

        GenericEntity<List<CapabilitiesResponse>> genericCapabilityResponsesList = new GenericEntity<List<CapabilitiesResponse>>(pair.getAttributesList()) {};
        return Response.status(OK).entity(genericCapabilityResponsesList).header(X_TOTAL_COUNT, pair.getXTotalCount()).build();
    }


    @GET
    @Path("capabilities/{capability_token:" + TOKEN_PATTERN + "}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapabilityInfo(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_token") String capabilityToken) {

        final CapabilitiesResponse capabilityResponses;

        try {
            capabilityResponses = Capabilities.getCapabilityInfo(capabilityToken);
        } catch (CapabilityNotFoundException e) {
            LOG.log(Level.WARNING, "Not Found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(OK).entity(capabilityResponses).build();
    }

    @GET
    @Secured
    @UserAccessSecured
    @Path("capabilities/{capability_token_id:" + ID_PATTERN + "}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapability(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_token_id") String capabilityTokenID) {

        final CapabilitiesResponse capabilityResponses;
        final long callingUserPk = ((KheopsPrincipalInterface)securityContext.getUserPrincipal()).getDBID();

        try {
            capabilityResponses = Capabilities.getCapability(capabilityTokenID, callingUserPk);
        } catch (CapabilityNotFoundException e) {
            LOG.log(Level.WARNING, "Not Found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(OK).entity(capabilityResponses).build();
    }
}
