package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipal;
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
                                        @FormParam("issued_at_time") String issuedAtTime,
                                        @NotNull @FormParam("scope_type") String scopeType,
                                        @FormParam("album") Long albumPk,
                                        @FormParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                        @FormParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                        @NotNull @FormParam("read_permission") boolean readPermission,
                                        @NotNull @FormParam("write_permission") boolean writePermission,
                                        @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        final CapabilitiesResponses.CapabilityResponse capabilityResponse;

        final CapabilityParametersBuilder capabilityParametersBuilder = new CapabilityParametersBuilder()
                .callingUserPk(callingUserPk)
                .title(title)
                .readPermission(readPermission)
                .writePermission(writePermission);
        if(issuedAtTime != null) {
            try {
            capabilityParametersBuilder.issuedAtTime(issuedAtTime);
            } catch (DateTimeParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Bad query parameter {issued_at_time}").build();
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
            capabilityParametersBuilder.scope(scopeType, albumPk, seriesInstanceUID, studyInstanceUID);
        } catch (CapabilityBadRequest e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{scope_type} = series or study or user or album. Not : "+scopeType).build();
        }

        CapabilityParameters capabilityParameters = capabilityParametersBuilder.build();

        try {
            capabilityResponse = generateCapability(capabilityParameters);
        } catch (UserNotFoundException | StudyNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
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

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
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
    public Response getCapabilities(@Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        List<CapabilityResponse> capabilityResponses;

        boolean showRevoke = false;
        if (uriInfo.getQueryParameters().containsKey("show_revoked")) {
            showRevoke = Boolean.parseBoolean(uriInfo.getQueryParameters().get("show_revoked").get(0));
        }

        try {
            capabilityResponses = Capabilities.getCapabilities(callingUserPk, showRevoke);
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        GenericEntity<List<CapabilityResponse>> genericCapabilityResponsesList = new GenericEntity<List<CapabilityResponse>>(capabilityResponses) {};
        return Response.status(Response.Status.OK).entity(genericCapabilityResponsesList).build();
    }
}
