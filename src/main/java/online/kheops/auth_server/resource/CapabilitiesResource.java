package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.*;
import online.kheops.auth_server.capability.CapabilitiesResponse.Response;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.format.DateTimeParseException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.capability.Capabilities.ID_PATTERN;
import static online.kheops.auth_server.capability.Capabilities.generateCapability;
import static online.kheops.auth_server.util.Consts.ALBUM;


@Path("/")
public class CapabilitiesResource {

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
    public javax.ws.rs.core.Response createNewCapability(@NotNull @FormParam("title") String title,
                                                         @FormParam("expiration_time") String expirationTime,
                                                         @FormParam("not_before_time") String notBeforeTime,
                                                         @NotNull @FormParam("scope_type") String scopeType,
                                                         @FormParam("album") String albumId,
                                                         @NotNull @FormParam("read_permission") boolean readPermission,
                                                         @NotNull @FormParam("appropriate_permission") boolean appropriatePermission,
                                                         @NotNull @FormParam("download_permission") boolean downloadPermission,
                                                         @NotNull @FormParam("write_permission") boolean writePermission) {

        final KheopsPrincipalInterface kheopsPrincipal = (KheopsPrincipalInterface) securityContext.getUserPrincipal();
        final Response capabilityResponse;

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
                return javax.ws.rs.core.Response.status(BAD_REQUEST).entity("Bad query parameter {not_before_time}").build();
            }
        }
        if(expirationTime != null) {
            try {
                capabilityParametersBuilder.expirationTime(expirationTime);

            } catch (DateTimeParseException e) {
                return javax.ws.rs.core.Response.status(BAD_REQUEST).entity("Bad query parameter {expiration_time}").build();
            }
        }

        try {
            capabilityParametersBuilder.scope(scopeType, albumId);
        } catch (CapabilityBadRequestException e) {
            return javax.ws.rs.core.Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return javax.ws.rs.core.Response.status(BAD_REQUEST).entity("{scope_type} = user or album. Not : "+scopeType).build();
        }

        CapabilityParameters capabilityParameters = capabilityParametersBuilder.build();

        try {
            capabilityResponse = generateCapability(capabilityParameters);
        } catch (UserNotFoundException | AlbumNotFoundException | UserNotMemberException e) {
            return javax.ws.rs.core.Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (NewCapabilityForbidden e) {
            return javax.ws.rs.core.Response.status(FORBIDDEN).entity(e.getMessage()).build();
        } catch (CapabilityBadRequestException e) {
            return javax.ws.rs.core.Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        return javax.ws.rs.core.Response.status(CREATED).entity(capabilityResponse).build();
    }

    @POST
    @Secured
    @CapabilitySecured
    @Path("capabilities/{capability_id:"+Capabilities.ID_PATTERN+"}/revoke")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response revokeCapability(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_id") String capabilityId) {

        final KheopsPrincipalInterface kheopsPrincipal = (KheopsPrincipalInterface)securityContext.getUserPrincipal();
        Response capabilityResponse;

        try {
            capabilityResponse = Capabilities.revokeCapability(kheopsPrincipal.getUser(), capabilityId);
        } catch (CapabilityNotFoundException e) {
            return javax.ws.rs.core.Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return javax.ws.rs.core.Response.status(OK).entity(capabilityResponse).build();
    }

    @GET
    @Secured
    @CapabilitySecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.MANAGE_CAPABILITIES_TOKEN)
    @Path("capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response getCapabilities(@QueryParam("valid") boolean valid,
                                                     @QueryParam(ALBUM) String albumId) {

        List<Response> capabilityResponses;

        if(albumId != null) {
            capabilityResponses = Capabilities.getCapabilities(albumId, valid);
        } else {
            final KheopsPrincipalInterface kheopsPrincipal = (KheopsPrincipalInterface)securityContext.getUserPrincipal();
            capabilityResponses = Capabilities.getCapabilities(kheopsPrincipal.getUser(), valid);
        }

        GenericEntity<List<Response>> genericCapabilityResponsesList = new GenericEntity<List<Response>>(capabilityResponses) {};
        return javax.ws.rs.core.Response.status(OK).entity(genericCapabilityResponsesList).build();
    }


    @GET
    @Path("capabilities/{capability_token:"+Capabilities.TOKEN_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response getCapabilityInfo(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_token") String capabilityToken) {

        Response capabilityResponses;

        try {
            capabilityResponses = Capabilities.getCapabilityInfo(capabilityToken);
        } catch (CapabilityNotFoundException e) {
            return javax.ws.rs.core.Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return javax.ws.rs.core.Response.status(OK).entity(capabilityResponses).build();
    }

    @GET
    @Secured
    @UserAccessSecured
    @Path("capabilities/{capability_token_id:"+ ID_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response getCapability(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_token_id") String capabilityTokenID) {

        Response capabilityResponses;

        final long callingUserPk = ((KheopsPrincipalInterface)securityContext.getUserPrincipal()).getDBID();

        try {
            capabilityResponses = Capabilities.getCapability(capabilityTokenID, callingUserPk);
        } catch (CapabilityNotFoundException e) {
            return javax.ws.rs.core.Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return javax.ws.rs.core.Response.status(OK).entity(capabilityResponses).build();
    }
}
