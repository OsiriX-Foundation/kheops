package online.kheops.auth_server.resource;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.Albums;
import online.kheops.auth_server.annotation.AlbumAccessSecured;
import online.kheops.auth_server.annotation.AlbumPermissionSecured;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.annotation.UserAccessSecured;
import online.kheops.auth_server.dicom_sr.DicomSrResponse;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.dicom_sr.DicomSrs.newDicomSr;
import static online.kheops.auth_server.util.Consts.ALBUM;


@Path("/")
public class DicomSrResource {
    private static final Logger LOG = Logger.getLogger(DicomSrResource.class.getName());

    @Context
    private UriInfo uriInfo;

    @Context
    private SecurityContext securityContext;

    @POST
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.CREATE_DICOM_SR)
    @Path("albums/{"+ALBUM+":"+ Albums.ID_PATTERN+"}/dicomsr")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addUser(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                            @FormParam("url") final String url,
                            @FormParam("name") final String name,
                            @FormParam("isPrivate") final boolean isPrivate) {

        //TODO vérifier les param (not empty not null etc)

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());


        final DicomSrResponse dicomSrResponse;
        try {
            dicomSrResponse = newDicomSr(kheopsPrincipal.getUser(), albumId, name, url, isPrivate);
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).build();
        }
        //Générer un client ID et un client Secret si besoin
        //nouvelle entrée dans la DB
        //retourner une response DICOM_SR JSON

        return Response.status(OK).entity(dicomSrResponse).build();
    }

}
