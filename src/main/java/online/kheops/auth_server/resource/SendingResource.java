package online.kheops.auth_server.resource;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.sharing.Sending;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UsersPermission;
import online.kheops.auth_server.util.Consts;

import java.util.logging.Logger;

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
                                       @QueryParam("album") Long fromAlbumPk,
                                       @QueryParam("inbox") Boolean fromInbox,
                                       @Context SecurityContext securityContext) {

        if ((fromAlbumPk != null && fromInbox != null)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        fromInbox = fromInbox == null && fromAlbumPk == null;
        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if(fromAlbumPk != null && !kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.SEND_SERIES, fromAlbumPk)) {
                fromAlbumPk = null;
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        if(fromInbox == true && !kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            fromInbox = false;
        }

        try {
            Sending.shareStudyWithUser(callingUserPk, username, studyInstanceUID, fromAlbumPk, fromInbox);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" with "+username);
        return Response.status(Response.Status.CREATED).build();
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

        try {
            if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.shareSeriesWithUser(callingUserPk, username, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" with "+username);
        return Response.status(Response.Status.CREATED).build();
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
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final Long albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.ADD_SERIES, albumID)) {
                        Sending.putSeriesInAlbum(callingUserPk, albumID, studyInstanceUID, seriesInstanceUID);
                } else {
                    return Response.status(Response.Status.FORBIDDEN).entity("todo write a good forbidden message").build();//TODO
                }
            } else {
                Sending.appropriateSeries(callingUserPk, studyInstanceUID, seriesInstanceUID);
            }
        } catch (UserNotFoundException | AlbumNotFoundException | NotAlbumScopeTypeException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(Response.Status.CREATED).build();
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
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (!kheopsPrincipal.hasUserAccess()) {
            try {
                return Response.status(Response.Status.BAD_REQUEST).entity("Use DELETE /studies/"+studyInstanceUID+"/album/"+kheopsPrincipal.getAlbumID()).build();
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        try {
            Sending.deleteStudyFromInbox(callingUserPk, studyInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" from user:" + callingUserPk);
        return Response.status(Response.Status.NO_CONTENT).build();
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
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        if (!kheopsPrincipal.hasUserAccess()) {
            try {
                return Response.status(Response.Status.BAD_REQUEST).entity("Use DELETE /studies/"+studyInstanceUID+"/album/"+kheopsPrincipal.getAlbumID()).build();
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        try {
            Sending.deleteSeriesFromInbox(callingUserPk, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from user:" + callingUserPk);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response putSeriesInAlbum(@PathParam("album") Long albumPk,
                                     @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                     @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                     @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();
        try {
            if (!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.ADD_SERIES, albumPk)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.putSeriesInAlbum(callingUserPk, albumPk, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+ "SeriesInstanceUID:"+seriesInstanceUID+" with albumPK "+albumPk);
        return Response.status(Response.Status.CREATED).build();

    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response putStudyInAlbum(@PathParam("album") Long albumPk,
                                    @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                    @QueryParam("album") Long fromAlbumPk,
                                    @QueryParam("inbox") Boolean fromInbox,
                                    @Context SecurityContext securityContext) {

        if ((fromAlbumPk != null && fromInbox != null)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        fromInbox = fromInbox != null;

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.ADD_SERIES, albumPk)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            if (fromAlbumPk != null && !kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.SEND_SERIES, fromAlbumPk)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Sending.putStudyInAlbum(callingUserPk, albumPk, studyInstanceUID, fromAlbumPk, fromInbox);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" with albumPK "+albumPk);
        return Response.status(Response.Status.CREATED).build();
    }


    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response deleteStudyFromAlbum(@PathParam("album") Long albumPk,
                                         @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                         @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.DELETE_SERIES, albumPk)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.deleteStudyFromAlbum(callingUserPk, albumPk, studyInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" from albumPK "+albumPk);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response deleteSeriesFromAlbum(@PathParam("album") Long albumPk,
                                          @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                          @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                          @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UsersPermission.UsersPermissionEnum.DELETE_SERIES, albumPk)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.deleteSeriesFromAlbum(callingUserPk, albumPk, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from albumPK "+albumPk);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}



