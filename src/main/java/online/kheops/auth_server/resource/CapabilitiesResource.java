package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.CapabilitySecured;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.capability.*;
import online.kheops.auth_server.capability.CapabilitiesResponses.CapabilityResponse;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.Consts;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.format.DateTimeParseException;
import java.util.List;

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
                                        @FormParam("album") Long albumPk,
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
                return Response.status(Response.Status.BAD_REQUEST).entity("Bad query parameter {not_before_time}").build();
            }
        }
        if(expirationTime != null) {
            try {
                capabilityParametersBuilder.expirationTime(expirationTime);

            } catch (DateTimeParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Bad query parameter {expiration_time}").build();
            }
        }

        try {
            capabilityParametersBuilder.scope(scopeType, albumPk);
        } catch (CapabilityBadRequest e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{scope_type} = user or album. Not : "+scopeType).build();
        }

        CapabilityParameters capabilityParameters = capabilityParametersBuilder.build();

        try {
            capabilityResponse = generateCapability(capabilityParameters);
        } catch (UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (NewCapabilityForbidden e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (CapabilityBadRequest e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.CREATED).entity(capabilityResponse).build();
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
        } catch (UserNotFoundException |CapabilityNotFound e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.OK).entity(capabilityResponse).build();
    }

    @GET
    @Secured
    @CapabilitySecured
    @Path("capabilities")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapabilities(@Context SecurityContext securityContext,
                                    @FormParam("show_revoked") boolean showRevoke ) {

        final long callingUserPk = ((KheopsPrincipalInterface)securityContext.getUserPrincipal()).getDBID();
        List<CapabilityResponse> capabilityResponses;
        
        try {
            capabilityResponses = Capabilities.getCapabilities(callingUserPk, showRevoke);
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        GenericEntity<List<CapabilityResponse>> genericCapabilityResponsesList = new GenericEntity<List<CapabilityResponse>>(capabilityResponses) {};
        return Response.status(Response.Status.OK).entity(genericCapabilityResponsesList).build();
    }
}
