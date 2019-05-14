package online.kheops.auth_server.resource;

import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.sharing.Sending;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
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
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.SEND_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareStudyWithUser(@PathParam("user") String username,
                                       @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                       @QueryParam(ALBUM) String fromAlbumId,
                                       @QueryParam(INBOX) Boolean fromInbox) {

        if ((fromAlbumId == null && fromInbox == null) ||
                (fromAlbumId != null && fromInbox != null && fromInbox)) {
            return Response.status(BAD_REQUEST).entity("Use only {"+ALBUM+"} xor {"+INBOX+"}").build();
        }

        if(fromAlbumId != null) {
            fromInbox = false;
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if(fromInbox && !kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            return Response.status(UNAUTHORIZED).entity("You can't send study:"+studyInstanceUID+" from inbox").build();
        }

        try {
            Sending.shareStudyWithUser(kheopsPrincipal.getUser(), username, studyInstanceUID, fromAlbumId, fromInbox);
        } catch (UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            LOG.log(WARNING, "not found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            LOG.log(WARNING, "bad request", e);
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
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

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(NOT_FOUND).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.shareSeriesWithUser(kheopsPrincipal.getUser(), username, studyInstanceUID, seriesInstanceUID);
        } catch (UserNotFoundException | SeriesNotFoundException e) {
            LOG.log(WARNING, "not found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished sharing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" with "+username);
        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response appropriateSeries(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                      @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                LOG.warning(() -> "Principal " + kheopsPrincipal + ", does not have write access");
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            LOG.log(WARNING, "StudyUID:" + studyInstanceUID + " SeriesUID:" + seriesInstanceUID + " was not found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.ADD_SERIES, albumID)) {
                    Sending.putSeriesInAlbum(kheopsPrincipal, albumID, studyInstanceUID, seriesInstanceUID);
                } else {
                    LOG.warning(() -> "Principal:" + kheopsPrincipal + " does not have write access to albumID:" + albumID);
                    return Response.status(FORBIDDEN).entity("No write access with this credential").build();
                }
            } else {
                Sending.appropriateSeries(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID);
            }
        } catch (AlbumNotFoundException | NotAlbumScopeTypeException | SeriesNotFoundException | ClientIdNotFoundException e) {
            LOG.log(WARNING, "Unable to add series", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.SEND_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response appropriateStudy(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                     @QueryParam(ALBUM) String albumId) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            return Response.status(FORBIDDEN).build();
        }

        try {
            if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.ADD_SERIES, albumID)) {
                    Sending.putStudyInAlbum(kheopsPrincipal, albumID, studyInstanceUID, albumId, false);
                } else {
                    return Response.status(FORBIDDEN).entity("No write access with this credential").build();
                }
            } else {
                Sending.appropriateStudy(kheopsPrincipal.getUser(), studyInstanceUID, albumId);
            }
        } catch (AlbumNotFoundException | NotAlbumScopeTypeException | SeriesNotFoundException e) {
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

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                String albumId = kheopsPrincipal.getAlbumID();
                if(kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.DELETE_SERIES,albumId)) {
                    Sending.deleteStudyFromAlbum(kheopsPrincipal, albumId, studyInstanceUID);
                    LOG.info(() -> "finished removing StudyInstanceUID:"+studyInstanceUID+" from albumId "+albumId);
                    return Response.status(NO_CONTENT).build();
                } else {
                    return Response.status(FORBIDDEN).build();
                }
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).build();
            } catch (AlbumNotFoundException | SeriesNotFoundException e) {
                return Response.status(NOT_FOUND).entity(e.getMessage()).build();
            }
        }

        try {
            Sending.deleteStudyFromInbox(kheopsPrincipal.getUser(), studyInstanceUID);
        } catch (SeriesNotFoundException e) {
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

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                String albumId = kheopsPrincipal.getAlbumID();
                if(kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.DELETE_SERIES,albumId)) {
                    Sending.deleteSeriesFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID);
                    LOG.info(() -> "finished removing StudyInstanceUID:" + studyInstanceUID + " SeriesInstanceUID:" + seriesInstanceUID + " from albumId " + albumId);
                    return Response.status(NO_CONTENT).build();
                } else {
                    return Response.status(FORBIDDEN).build();
                }
            } catch (NotAlbumScopeTypeException e) {
                LOG.log(WARNING, "bad scope type", e);
                return Response.status(BAD_REQUEST).build();
            } catch (AlbumNotFoundException | SeriesNotFoundException e) {
                LOG.log(WARNING, "not found", e);
                return Response.status(NOT_FOUND).entity(e.getMessage()).build();
            }
        }

        try {
            Sending.deleteSeriesFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID);
        } catch (SeriesNotFoundException e) {
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
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response putSeriesInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                     @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                     @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.putSeriesInAlbum(kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID);
        } catch (AlbumNotFoundException | ClientIdNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished sharing StudyInstanceUID:"+studyInstanceUID+ "SeriesInstanceUID:"+seriesInstanceUID+" with albumID "+albumId);
        return Response.status(CREATED).build();

    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}")
    public Response putStudyInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                    @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                    @QueryParam(ALBUM) String fromAlbumId,
                                    @QueryParam(INBOX) Boolean fromInbox) {

        if ((fromAlbumId == null && fromInbox == null) ||
                (fromAlbumId != null && fromInbox != null && fromInbox)) {
            return Response.status(BAD_REQUEST).entity("Use only {"+ALBUM+"} xor {"+INBOX+"}").build();
        }

        if(fromAlbumId != null) {
            fromInbox = false;
        }

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            if (!kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.ADD_SERIES, albumId)) {
                return Response.status(FORBIDDEN).build();
            }

            if (fromAlbumId != null && !kheopsPrincipal.hasAlbumPermission(UserPermissionEnum.SEND_SERIES, fromAlbumId)) {
                return Response.status(FORBIDDEN).build();
            }
        } catch (AlbumNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        try {
            Sending.putStudyInAlbum(kheopsPrincipal, albumId, studyInstanceUID, fromAlbumId, fromInbox);
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished sharing StudyInstanceUID:"+studyInstanceUID+" with albumId "+albumId);
        return Response.status(CREATED).build();
    }


    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.DELETE_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response deleteStudyFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                         @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID) {

        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            Sending.deleteStudyFromAlbum(kheopsPrincipal, albumId, studyInstanceUID);
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished removing StudyInstanceUID:"+studyInstanceUID+" from albumId "+albumId);
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(UserPermissionEnum.DELETE_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response deleteSeriesFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                          @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                          @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {


        final KheopsPrincipalInterface kheopsPrincipal = ((KheopsPrincipalInterface)securityContext.getUserPrincipal());

        try {
            Sending.deleteSeriesFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID);
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info(() -> "finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from albumId "+albumId);
        return Response.status(NO_CONTENT).build();
    }
}
