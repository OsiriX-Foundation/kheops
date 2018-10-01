package online.kheops.auth_server.resource;

import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.CapabilitySecured;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.capability.Capabilities;
import online.kheops.auth_server.capability.CapabilitiesResponses;
import online.kheops.auth_server.capability.CapabilitiesResponses.CapabilityResponse;
import online.kheops.auth_server.capability.CapabilityNotFound;
import online.kheops.auth_server.capability.NewCapabilityForbidden;
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
import static online.kheops.auth_server.series.Series.checkValidUID;


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
                                        @FormParam("expiration") String expirationDate,
                                        @NotNull @FormParam("scope_type") String scopeType,
                                        @FormParam("album") Long albumPk,
                                        @FormParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                        @FormParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                        @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        CapabilitiesResponses.CapabilityResponse capabilityResponse;

        if (scopeType.compareTo("album") != 0 ||scopeType.compareTo("series") != 0 ||scopeType.compareTo("study") != 0 || scopeType.compareTo("user") != 0) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{scope_type} = series or study or user or album").build();
        }

        try {
            switch (scopeType) {
                case "user":
                    capabilityResponse = createUserCapability(callingUserPk, title, expirationDate);
                    break;
                case "album":
                    if( albumPk == null) {
                        return Response.status(Response.Status.BAD_REQUEST).entity("The {album} query parameter must be set").build();
                    }
                    capabilityResponse = createAlbumCapability(callingUserPk, title, expirationDate, albumPk);
                    break;
                case "series":
                    if( seriesInstanceUID == null && studyInstanceUID == null) {
                        return Response.status(Response.Status.BAD_REQUEST).entity("The {series} and {study} query parameters must be set").build();
                    }
                    checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);
                    checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
                    capabilityResponse = createSeriesCapability(callingUserPk, title, expirationDate, studyInstanceUID, seriesInstanceUID);
                    break;
                case "study":
                    if( studyInstanceUID == null) {
                        return Response.status(Response.Status.BAD_REQUEST).entity("The {study} query parameter must be set").build();
                    }
                    checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
                    capabilityResponse = createStudyCapability(callingUserPk, title, expirationDate, studyInstanceUID);
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST).entity("{scope_type} = series or study or user or album").build();
            }
        } catch (UserNotFoundException | StudyNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Bad query parameter {expiration}").build();
        } catch (NewCapabilityForbidden e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.CREATED).entity(capabilityResponse).build();
    }

    @POST
    @Secured
    @CapabilitySecured
    @Path("capabilities/{secret:[a-zA-Z0-9]{22}}/revoke")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revokeCapability(@PathParam("secret") String secret,
                                     @Context SecurityContext securityContext) {

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();
        CapabilityResponse capabilityResponse;

        try {
            capabilityResponse = Capabilities.revokeCapability(callingUserPk, secret);
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
