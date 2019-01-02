package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.CapabilitySecured;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.capability.*;
import online.kheops.auth_server.capability.CapabilitiesResponses.CapabilityResponse;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.format.DateTimeParseException;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.capability.Capabilities.*;


@Path("/")
public class CapabilitiesResource {

    @Context
    private UriInfo uriInfo;

    @POST
    @Secured
    @CapabilitySecured
    @FormURLEncodedContentType
    @Path("capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewCapability(@NotNull @FormParam("title") String title,
                                        @FormParam("expiration_time") String expirationTime,
                                        @FormParam("not_before_time") String notBeforeTime,
                                        @NotNull @FormParam("scope_type") String scopeType,
                                        @FormParam("album") String albumId,
                                        @NotNull @FormParam("read_permission") boolean readPermission,
                                        @NotNull @FormParam("appropriate_permission") boolean appropriatePermission,
                                        @NotNull @FormParam("download_permission") boolean downloadPermission,
                                        @NotNull @FormParam("write_permission") boolean writePermission,
                                        @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipalInterface)securityContext.getUserPrincipal()).getDBID();
        final CapabilitiesResponses.CapabilityResponse capabilityResponse;

        final CapabilityParametersBuilder capabilityParametersBuilder = new CapabilityParametersBuilder()
                .callingUserPk(callingUserPk)
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

        CapabilityParameters capabilityParameters = capabilityParametersBuilder.build();

        try {
            capabilityResponse = generateCapability(capabilityParameters);
        } catch (UserNotFoundException | AlbumNotFoundException | UserNotMemberException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (NewCapabilityForbidden e) {
            return Response.status(FORBIDDEN).entity(e.getMessage()).build();
        } catch (CapabilityBadRequestException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.status(CREATED).entity(capabilityResponse).build();
    }

    @POST
    @Secured
    @CapabilitySecured
    @Path("capabilities/{capability_id:[1-9][0-9]*}/revoke")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeCapability(@PathParam("capability_id") long capabilityId,
                                     @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipalInterface)securityContext.getUserPrincipal()).getDBID();
        CapabilityResponse capabilityResponse;

        try {
            capabilityResponse = Capabilities.revokeCapability(callingUserPk, capabilityId);
        } catch (UserNotFoundException |CapabilityNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(OK).entity(capabilityResponse).build();
    }

    @GET
    @Secured
    @CapabilitySecured
    @Path("capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapabilities(@Context SecurityContext securityContext,
                                    @QueryParam("show_revoked") boolean showRevoke ) {

        final long callingUserPk = ((KheopsPrincipalInterface)securityContext.getUserPrincipal()).getDBID();
        List<CapabilityResponse> capabilityResponses;

        try {
            capabilityResponses = Capabilities.getCapabilities(callingUserPk, showRevoke);
        } catch (UserNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        GenericEntity<List<CapabilityResponse>> genericCapabilityResponsesList = new GenericEntity<List<CapabilityResponse>>(capabilityResponses) {};
        return Response.status(OK).entity(genericCapabilityResponsesList).build();
    }

    @GET
    @Path("capabilities/{capability_token:"+Capabilities.TOKEN_PATTERN+"}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapabilityInfo(@SuppressWarnings("RSReferenceInspection") @PathParam("capability_token") String capabilityToken,
                                      @Context SecurityContext securityContext) {

        CapabilityResponse capabilityResponses;

        try {
            capabilityResponses = Capabilities.getCapabilityInfo(capabilityToken);
        } catch (CapabilityNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(OK).entity(capabilityResponses).build();
    }
}
