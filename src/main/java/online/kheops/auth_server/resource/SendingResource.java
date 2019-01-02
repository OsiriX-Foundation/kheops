package online.kheops.auth_server.resource;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.Albums;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.series.SeriesForbiddenException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.sharing.Sending;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.util.Consts;

import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.series.Series.checkValidUID;

@Path("/")
public class SendingResource
{

    private static final Logger LOG = Logger.getLogger(SendingResource.class.getName());

    @Context
    ServletContext context;

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareStudyWithUser(@PathParam("user") String username,
                                       @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                       @QueryParam("album") String fromAlbumId,
                                       @QueryParam("inbox") Boolean fromInbox,
                                       @Context SecurityContext securityContext) {

        if ((fromAlbumId != null && fromInbox != null)) {
            return Response.status(BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        fromInbox = fromInbox == null && fromAlbumId == null;
        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        if (!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if(fromAlbumId != null && !kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.SEND_SERIES, fromAlbumId)) {
                fromAlbumId = null;
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        if(fromInbox == true && !kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            fromInbox = false;
        }

        try {
            Sending.shareStudyWithUser(callingUserPk, username, studyInstanceUID, fromAlbumId, fromInbox);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" with "+username);
        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareSeriesWithUser(@PathParam("user") String username,
                                        @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                        @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                        @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        if (!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(NOT_FOUND).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.shareSeriesWithUser(callingUserPk, username, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" with "+username);
        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response putSeries(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                              @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                              @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.ADD_SERIES, albumID)) {
                        Sending.putSeriesInAlbum(callingUserPk, albumID, studyInstanceUID, seriesInstanceUID);
                } else {
                    return Response.status(FORBIDDEN).entity("todo write a good forbidden message").build();//TODO
                }
            } else {
                Sending.appropriateSeries(callingUserPk, studyInstanceUID, seriesInstanceUID);
            }
        } catch (UserNotFoundException | AlbumNotFoundException | NotAlbumScopeTypeException | SeriesForbiddenException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(CREATED).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteStudyFromInbox(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                         @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            return Response.status(FORBIDDEN).build();
        }

        if (!kheopsPrincipal.hasUserAccess()) {
            try {
                return this.deleteStudyFromAlbum(kheopsPrincipal.getAlbumID(), studyInstanceUID, securityContext);
                //return Response.status(BAD_REQUEST).entity("Use DELETE /studies/"+studyInstanceUID+"/album/"+kheopsPrincipal.getAlbumID()).build();
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).build();
            }
        }

        try {
            Sending.deleteStudyFromInbox(callingUserPk, studyInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" from user:" + callingUserPk);
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteSeriesFromInbox(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                          @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                          @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try{
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        if (!kheopsPrincipal.hasUserAccess()) {
            try {
                return this.deleteSeriesFromAlbum(kheopsPrincipal.getAlbumID(), studyInstanceUID, seriesInstanceUID, securityContext);
                //return Response.status(BAD_REQUEST).entity("Use DELETE /studies/"+studyInstanceUID+"/series/"+seriesInstanceUID+"/album/"+kheopsPrincipal.getAlbumID()).build();
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).build();
            }
        }

        try {
            Sending.deleteSeriesFromInbox(callingUserPk, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from user:" + callingUserPk);
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:"+Albums.ID_PATTERN+"}")
    public Response putSeriesInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam("album") String albumId,
                                     @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                     @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                     @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        if (!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.ADD_SERIES, albumId)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.putSeriesInAlbum(callingUserPk, albumId, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+ "SeriesInstanceUID:"+seriesInstanceUID+" with albumPK "+albumId);
        return Response.status(CREATED).build();

    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:"+ Albums.ID_PATTERN+"}")
    public Response putStudyInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam("album") String albumId,
                                    @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                    @QueryParam("album") String fromAlbumId,
                                    @QueryParam("inbox") Boolean fromInbox,
                                    @Context SecurityContext securityContext) {

        if ((fromAlbumId != null && fromInbox != null)) {
            return Response.status(BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        fromInbox = fromInbox != null;

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasUserAccess()) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.ADD_SERIES, albumId)) {
                return Response.status(FORBIDDEN).build();
            }

            if (fromAlbumId != null && !kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.SEND_SERIES, fromAlbumId)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Sending.putStudyInAlbum(callingUserPk, albumId, studyInstanceUID, fromAlbumId, fromInbox);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" with albumPK "+albumId);
        return Response.status(CREATED).build();
    }


    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:"+Albums.ID_PATTERN+"}")
    public Response deleteStudyFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam("album") String albumId,
                                         @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                         @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.DELETE_SERIES, albumId)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.deleteStudyFromAlbum(callingUserPk, albumId, studyInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" from albumPK "+albumId);
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:"+Albums.ID_PATTERN+"}")
    public Response deleteSeriesFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam("album") String albumId,
                                          @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                          @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                          @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.DELETE_SERIES, albumId)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.deleteSeriesFromAlbum(callingUserPk, albumId, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from albumPK "+albumId);
        return Response.status(NO_CONTENT).build();
    }
}



