package online.kheops.auth_server.resource;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.series.SeriesForbiddenException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.sharing.Sending;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;

import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.util.Consts.*;

@Path("/")
public class SendingResource
{

    private static final Logger LOG = Logger.getLogger(SendingResource.class.getName());

    @Context
    ServletContext context;

    @Context
    private SecurityContext securityContext;

    @PUT
    @Secured
    @UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareStudyWithUser(@PathParam("user") String username,
                                       @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                       @QueryParam(ALBUM) Long fromAlbumPk,
                                       @QueryParam(INBOX) Boolean fromInbox) {

        if ((fromAlbumPk != null && fromInbox != null)) {
            return Response.status(BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        fromInbox = fromInbox == null && fromAlbumPk == null;

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if(fromAlbumPk != null && !kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.SEND_SERIES, fromAlbumPk)) {
                fromAlbumPk = null;
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        if(fromInbox == true && !kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            fromInbox = false;
        }

        try {
            Sending.shareStudyWithUser(callingUserPk, username, studyInstanceUID, fromAlbumPk, fromInbox);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished sharing StudyInstanceUID:"+studyInstanceUID+" with "+username);
        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareSeriesWithUser(@PathParam("user") String username,
                                        @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                        @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

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

        LOG.info(() -> "finished sharing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" with "+username);
        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response putSeries(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                              @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

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
                final Long albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.ADD_SERIES, albumID)) {
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
    //@UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteStudyFromInbox(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            return Response.status(FORBIDDEN).build();
        }

        if (!kheopsPrincipal.hasUserAccess()) {
            try {
                return this.deleteStudyFromAlbum(kheopsPrincipal.getAlbumID(), studyInstanceUID);
                //return Response.status(BAD_REQUEST).entity("Use DELETE /studies/"+studyInstanceUID+"/album/"+kheopsPrincipal.getAlbumID()).build();
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).build();
            }
        }

        try {
            Sending.deleteStudyFromInbox(kheopsPrincipal.getUser(), studyInstanceUID);
        } catch(SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info(() -> "finished removing StudyInstanceUID:"+studyInstanceUID+" from user:" + kheopsPrincipal.getDBID());
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    //@UserAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteSeriesFromInbox(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                          @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try{
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        if (!kheopsPrincipal.hasUserAccess()) {
            try {
                return this.deleteSeriesFromAlbum(kheopsPrincipal.getAlbumID(), studyInstanceUID, seriesInstanceUID);
                //return Response.status(BAD_REQUEST).entity("Use DELETE /studies/"+studyInstanceUID+"/series/"+seriesInstanceUID+"/album/"+kheopsPrincipal.getAlbumID()).build();
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).build();
            }
        }

        try {
            Sending.deleteSeriesFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID);
        } catch(SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info(() -> "finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from user:" + kheopsPrincipal.getDBID());
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.ADD_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response putSeriesInAlbum(@PathParam("album") Long albumPk,
                                     @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                     @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.putSeriesInAlbum(callingUserPk, albumPk, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished sharing StudyInstanceUID:"+studyInstanceUID+ "SeriesInstanceUID:"+seriesInstanceUID+" with albumPK "+albumPk);
        return Response.status(CREATED).build();

    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response putStudyInAlbum(@PathParam("album") Long albumPk,
                                    @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                    @QueryParam(ALBUM) Long fromAlbumPk,
                                    @QueryParam(INBOX) Boolean fromInbox) {

        if ((fromAlbumPk != null && fromInbox != null)) {
            return Response.status(BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        fromInbox = fromInbox != null;

        KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.ADD_SERIES, albumPk)) {
                return Response.status(FORBIDDEN).build();
            }

            if (fromAlbumPk != null && !kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.SEND_SERIES, fromAlbumPk)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        final long callingUserPk = kheopsPrincipal.getDBID();

        try {
            Sending.putStudyInAlbum(callingUserPk, albumPk, studyInstanceUID, fromAlbumPk, fromInbox);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished sharing StudyInstanceUID:"+studyInstanceUID+" with albumPK "+albumPk);
        return Response.status(CREATED).build();
    }


    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.DELETE_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response deleteStudyFromAlbum(@PathParam("album") Long albumPk,
                                         @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            Sending.deleteStudyFromAlbum(kheopsPrincipal.getUser(), albumPk, studyInstanceUID);
        } catch(AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished removing StudyInstanceUID:"+studyInstanceUID+" from albumPK "+albumPk);
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.DELETE_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response deleteSeriesFromAlbum(@PathParam("album") Long albumPk,
                                          @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                          @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {


        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            Sending.deleteSeriesFromAlbum(kheopsPrincipal.getUser(), albumPk, studyInstanceUID, seriesInstanceUID);
        } catch(AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from albumPK "+albumPk);
        return Response.status(NO_CONTENT).build();
    }
}



