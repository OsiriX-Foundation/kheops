package online.kheops.auth_server.resource;

import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.accesstoken.AccessToken;
import online.kheops.auth_server.accesstoken.AccessTokenVerificationException;
import online.kheops.auth_server.accesstoken.AccessTokenVerifier;
import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.CapabilityPrincipal;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.sharing.Sending;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.AlbumUserPermissions;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.HttpHeaders.X_TOKEN_SOURCE;

@Path("/")
public class SendingResource
{

    private static final Logger LOG = Logger.getLogger(SendingResource.class.getName());

    @Context
    private ServletContext context;

    @Context
    private SecurityContext securityContext;

    @HeaderParam(X_TOKEN_SOURCE)
    String headerXTokenSource;

    @PUT
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(AlbumUserPermissions.SEND_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareStudyWithUser(@PathParam("user") String username,
                                       @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                       @QueryParam(ALBUM) String fromAlbumId,
                                       @QueryParam(INBOX) Boolean fromInbox,
                                       @Context HttpServletRequest request,
                                       @Context HttpServletResponse response) {


        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                if (fromInbox != null || (fromAlbumId != null && !fromAlbumId.equals(kheopsPrincipal.getAlbumID()))) {
                    return Response.status(BAD_REQUEST).build();
                } else if (fromAlbumId == null) {
                    request.getRequestDispatcher("/studies/" + studyInstanceUID + "/users/" + username + "?&album=" + kheopsPrincipal.getAlbumID()).forward(request, response);
                    return Response.status(response.getStatus()).entity(response.getOutputStream()).build();
                }
            } catch (NotAlbumScopeTypeException | AlbumNotFoundException | IOException | ServletException e) {
                return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
            }
        }

        if ((fromAlbumId == null && fromInbox == null) ||
                (fromAlbumId != null && fromInbox != null && fromInbox)) {
            return Response.status(BAD_REQUEST).entity("Use only {"+ALBUM+"} xor {"+INBOX+"}").build();
        }

        if(fromAlbumId != null) {
            fromInbox = false;
        }

        if(fromInbox && !kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            return Response.status(UNAUTHORIZED).entity("You can't send study:"+studyInstanceUID+" from inbox").build();
        }

        try {
            Sending.shareStudyWithUser(kheopsPrincipal.getUser(), username, studyInstanceUID, fromAlbumId, fromInbox, kheopsPrincipal.getKheopsLogBuilder());
        } catch (UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        }

        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareSeriesWithUser(@PathParam("user") String username,
                                        @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                        @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
            return Response.status(FORBIDDEN ).build();
        }

        try {
            Sending.shareSeriesWithUser(kheopsPrincipal.getUser(), username, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (UserNotFoundException | SeriesNotFoundException e) {
            LOG.log(WARNING, "not found", e);
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response appropriateSeries(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                      @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        boolean fromToken;

        if (headerXTokenSource != null) {
            fromToken = true;
            final KheopsPrincipal tokenPrincipal;
            try {
                tokenPrincipal = getPrincipalFromHeadersXTokenSource(headerXTokenSource);
            } catch (AccessTokenVerificationException e) {
                return Response.status(FORBIDDEN).entity(e.getMessage()).build();
            } catch (UserNotFoundException e) {
                return Response.status(FORBIDDEN).build();
            }

            if (!tokenPrincipal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }

            if (tokenPrincipal.getClass() != CapabilityPrincipal.class) {
                return Response.status(FORBIDDEN).build();
            }

            final Optional<Capability> optionalCapability = tokenPrincipal.getCapability();
            if (optionalCapability.isPresent()) {
                final Capability capability = optionalCapability.get();
                if (!capability.hasAppropriatePermission()) {
                    return Response.status(FORBIDDEN).build();
                }
            }

        } else {
            fromToken = false;
            if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                LOG.warning(() -> "Principal " + kheopsPrincipal + ", does not have write access");
                return Response.status(FORBIDDEN).build();
            }
        }

        try {
            if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(AlbumUserPermissions.ADD_SERIES, albumID)) {
                    Sending.putSeriesInAlbum(kheopsPrincipal, albumID, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
                } else {
                    LOG.warning(() -> "Principal:" + kheopsPrincipal + " does not have write access to albumID:" + albumID);
                    return Response.status(FORBIDDEN).entity("No write access with this credential").build();
                }
            } else {
                Sending.appropriateSeries(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, fromToken, kheopsPrincipal.getKheopsLogBuilder());
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
    @AlbumPermissionSecured(AlbumUserPermissions.SEND_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response appropriateStudy(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                     @QueryParam(ALBUM) String albumId) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (headerXTokenSource != null) {
            final KheopsPrincipal tokenPrincipal;
            try {
                tokenPrincipal = getPrincipalFromHeadersXTokenSource(headerXTokenSource);
            } catch (AccessTokenVerificationException e) {
                return Response.status(FORBIDDEN).entity(e.getMessage()).build();
            } catch (UserNotFoundException e) {
                return Response.status(FORBIDDEN).build();
            }

            if (!tokenPrincipal.hasStudyReadAccess(studyInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }

            if (tokenPrincipal.getClass() != CapabilityPrincipal.class) {
                return Response.status(FORBIDDEN).build();
            }

            final Optional<Capability> optionalCapability = tokenPrincipal.getCapability();
            if (optionalCapability.isPresent()) {
                final Capability capability = optionalCapability.get();
                if (!capability.hasAppropriatePermission()) {
                    return Response.status(FORBIDDEN).build();
                }
                if (albumId != null) {
                    return Response.status(BAD_REQUEST).build();
                }
                albumId = capability.getAlbum().getId();
            }

        } else {
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        }

        try {
            if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(AlbumUserPermissions.ADD_SERIES, albumID)) {
                    Sending.putStudyInAlbum(kheopsPrincipal, albumID, studyInstanceUID, albumId, false, kheopsPrincipal.getKheopsLogBuilder());
                } else {
                    return Response.status(FORBIDDEN).entity("No write access with this credential").build();
                }
            } else {
                Sending.appropriateStudy(kheopsPrincipal.getUser(), studyInstanceUID, albumId, kheopsPrincipal.getKheopsLogBuilder());
            }
        } catch (AlbumNotFoundException | NotAlbumScopeTypeException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(CREATED).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteStudyFromInbox(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                         @Context HttpServletRequest request,
                                         @Context HttpServletResponse response) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                request.getRequestDispatcher("/studies/" + studyInstanceUID + "/albums/"+kheopsPrincipal.getAlbumID()).forward(request, response);
                return Response.status(response.getStatus()).entity(response.getOutputStream()).build();
            } catch (NotAlbumScopeTypeException | AlbumNotFoundException | IOException | ServletException e) {
                return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
            }
        }

        if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            return Response.status(FORBIDDEN).build();
        }

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                String albumId = kheopsPrincipal.getAlbumID();
                if(kheopsPrincipal.hasAlbumPermission(AlbumUserPermissions.DELETE_SERIES,albumId)) {
                    Sending.deleteStudyFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
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
            Sending.deleteStudyFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteSeriesFromInbox(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                          @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID,
                                          @Context HttpServletRequest request,
                                          @Context HttpServletResponse response) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                request.getRequestDispatcher("/studies/" + studyInstanceUID + "/series/" + seriesInstanceUID + "/albums/"+kheopsPrincipal.getAlbumID()).forward(request, response);
                return Response.status(response.getStatus()).entity(response.getOutputStream()).build();
            } catch (NotAlbumScopeTypeException | AlbumNotFoundException | IOException | ServletException e) {
                return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
            }
        }

        if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
            return Response.status(FORBIDDEN).build();
        }

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                String albumId = kheopsPrincipal.getAlbumID();
                if(kheopsPrincipal.hasAlbumPermission(AlbumUserPermissions.DELETE_SERIES,albumId)) {
                    Sending.deleteSeriesFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
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
            Sending.deleteSeriesFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(AlbumUserPermissions.ADD_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response putSeriesInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                     @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                     @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (headerXTokenSource != null) {
            final KheopsPrincipal tokenPrincipal;
            try {
                tokenPrincipal = getPrincipalFromHeadersXTokenSource(headerXTokenSource);
            } catch (AccessTokenVerificationException e) {
                return Response.status(FORBIDDEN).entity(e.getMessage()).build();
            } catch (UserNotFoundException e) {
                return Response.status(FORBIDDEN).build();
            }

            if (!tokenPrincipal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }

            if (tokenPrincipal.getClass() != CapabilityPrincipal.class) {
                return Response.status(FORBIDDEN).build();
            }
            final Optional<Capability> optionalCapability = tokenPrincipal.getCapability();
            if (optionalCapability.isPresent()) {
                final Capability capability = optionalCapability.get();
                if (!capability.hasAppropriatePermission()) {
                    return Response.status(FORBIDDEN).build();
                }
            }

        } else {
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                return Response.status(FORBIDDEN).build();
            }
        }

        try {
            Sending.putSeriesInAlbum(kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (AlbumNotFoundException | ClientIdNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(CREATED).build();

    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(AlbumUserPermissions.ADD_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}")
    public Response putStudyInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                    @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                    @QueryParam(ALBUM) String fromAlbumId,
                                    @QueryParam(INBOX) Boolean fromInbox) {


        if (((fromAlbumId == null && fromInbox == null) ||
                (fromAlbumId != null && fromInbox != null && fromInbox)) && headerXTokenSource == null) {
            return Response.status(BAD_REQUEST).entity("Use only {"+ALBUM+"} xor {"+INBOX+"}").build();
        }

        if(fromAlbumId != null) {
            fromInbox = false;
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (headerXTokenSource != null) {
            final KheopsPrincipal tokenPrincipal;
            try {
                tokenPrincipal = getPrincipalFromHeadersXTokenSource(headerXTokenSource);
            } catch (AccessTokenVerificationException e) {
                return Response.status(FORBIDDEN).entity(e.getMessage()).build();
            } catch (UserNotFoundException e) {
                return Response.status(FORBIDDEN).build();
            }

            if (tokenPrincipal.getScope() == ScopeType.ALBUM)
            {
                try {
                    fromAlbumId = tokenPrincipal.getAlbumID();
                    if (!tokenPrincipal.hasAlbumPermission(AlbumUserPermissions.SEND_SERIES, fromAlbumId)) {
                        return Response.status(FORBIDDEN).build();
                    }
                    if (!tokenPrincipal.hasStudyReadAccess(studyInstanceUID)) {
                        return Response.status(FORBIDDEN).build();
                    }

                } catch (AlbumNotFoundException | NotAlbumScopeTypeException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                return Response.status(FORBIDDEN).build();
            }

        } else {
            if (!kheopsPrincipal.hasAlbumPermission(AlbumUserPermissions.ADD_SERIES, albumId)) {
                return Response.status(FORBIDDEN).build();
            }

            if (fromAlbumId != null && !kheopsPrincipal.hasAlbumPermission(AlbumUserPermissions.SEND_SERIES, fromAlbumId)) {
                return Response.status(FORBIDDEN).build();
            }
        }

        try {
            Sending.putStudyInAlbum(kheopsPrincipal, albumId, studyInstanceUID, fromAlbumId, fromInbox, kheopsPrincipal.getKheopsLogBuilder());
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(CREATED).build();
    }


    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(AlbumUserPermissions.DELETE_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response deleteStudyFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                         @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        try {
            Sending.deleteStudyFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(AlbumUserPermissions.DELETE_SERIES)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response deleteSeriesFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                          @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                          @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        try {
            Sending.deleteSeriesFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    private KheopsPrincipal getPrincipalFromHeadersXTokenSource(String token)
            throws AccessTokenVerificationException, UserNotFoundException {

        final AccessToken accessToken = AccessTokenVerifier.authenticateAccessToken(context, token, false);
        final User user = getOrCreateUser(accessToken.getSubject());

        return accessToken.newPrincipal(context, user);
    }
}
