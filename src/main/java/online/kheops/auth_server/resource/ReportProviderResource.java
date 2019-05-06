package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.AlbumAccessSecured;
import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.report_provider.ReportProviderResponse;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.report_provider.ReportProviders.newReportProvider;
import static online.kheops.auth_server.util.Consts.ALBUM;


@Path("/")
public class ReportProviderResource {
    private static final Logger LOG = Logger.getLogger(ReportProviderResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.CREATE_DICOM_SR)
    @Path("albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}/reportprovider")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addUser(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                            @FormParam("url") final String url,
                            @FormParam("name") final String name) {

        if (name == null || name.isEmpty()) {
            return Response.status(BAD_REQUEST).entity("'name' formparam must be set").build();
        }
        if (url == null || url.isEmpty()) {
            return Response.status(BAD_REQUEST).entity("'url' formparam must be set").build();
        } else {
            try {
                new URI(url);
            } catch (URISyntaxException e) {
                return Response.status(BAD_REQUEST).entity("'url' formparam not valid").build();

            }
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        final ReportProviderResponse dicomSrResponse;
        try {
            dicomSrResponse = newReportProvider(kheopsPrincipal.getUser(), albumId, name, url);
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }

        return Response.status(OK).entity(dicomSrResponse).build();
    }


    /*
    TODO
    GET /api/albums/{album_id}/reportprovider

    GET /api/albums/{album_id}/reportprovider/{client_id}

    DELETE /api/albums/{album_id}/reportprovider/{client_id}

    PATCH  /api/albums/{album_id}/reportprovider/{client_id}
    */

}
