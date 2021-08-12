package online.kheops.auth_server.resource;

import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.accesstoken.AccessToken;
import online.kheops.auth_server.accesstoken.AccessTokenVerificationException;
import online.kheops.auth_server.accesstoken.AccessTokenVerifier;
import online.kheops.auth_server.album.AlbumId;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.CapabilityPrincipal;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.sharing.Sending;
import online.kheops.auth_server.webhook.delayed_webhook.DelayedWebhook;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;

import javax.inject.Inject;
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
import static javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.PATH_PARAM;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.QUERY_PARAM;
import static online.kheops.auth_server.filter.SecuredFilter.getToken;
import static online.kheops.auth_server.user.AlbumUserPermissions.*;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.ErrorResponse.Message.*;
import static online.kheops.auth_server.util.HttpHeaders.X_AUTHORIZATION_SOURCE;

@Path("/")
public class SendingResource
{

    private static final Logger LOG = Logger.getLogger(SendingResource.class.getName());

    @Context
    private ServletContext context;

    @Context
    private SecurityContext securityContext;

    @Inject
    DelayedWebhook delayedWebhook;

    @HeaderParam(X_AUTHORIZATION_SOURCE)
    private String headerXAuthorizationSource;


    @PUT
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = SEND_SERIES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareStudyWithUser(@PathParam("user") String username,
                                       @PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                       @QueryParam(ALBUM) String fromAlbumId,
                                       @QueryParam(INBOX) Boolean fromInbox,
                                       @Context HttpServletRequest request,
                                       @Context HttpServletResponse response)
            throws AlbumNotFoundException, SeriesNotFoundException, UserNotFoundException {


        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                if (fromInbox != null || (fromAlbumId != null && !fromAlbumId.equals(kheopsPrincipal.getAlbumID()))) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("With an album capability token, 'inbox' and 'album' must not be set")
                            .build();
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                } else if (fromAlbumId == null) {
                    request.getRequestDispatcher("/studies/" + studyInstanceUID + "/users/" + username + "?&album=" + kheopsPrincipal.getAlbumID()).forward(request, response);
                    return Response.status(response.getStatus()).entity(response.getOutputStream()).build();
                }
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
            } catch (IOException | ServletException e) {
                LOG.log(WARNING, "Bad Request", e);
                return Response.status(BAD_REQUEST).build();
            }
        }

        if ((fromAlbumId == null && fromInbox == null) ||
                (fromAlbumId != null && fromInbox != null && fromInbox)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Use only '"+ALBUM+"' xor '"+INBOX+"' not both")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        if(fromAlbumId != null) {
            fromInbox = false;
        }

        if(Boolean.TRUE.equals(fromInbox) && !kheopsPrincipal.hasStudyShareAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("Study not found in the inbox")
                    .build();
            return Response.status(NOT_FOUND).entity(errorResponse).build();
        }

        try {
            Sending.shareStudyWithUser(kheopsPrincipal.getUser(), username, studyInstanceUID, fromAlbumId, fromInbox, kheopsPrincipal.getKheopsLogBuilder());
        } catch (BadRequestException e) {
            LOG.log(WARNING, "Bad request", e);
            return Response.status(BAD_REQUEST).build();
        }

        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareSeriesWithUser(@PathParam("user") String username,
                                        @PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                        @PathParam(SERIES_INSTANCE_UID) @UIDValidator String seriesInstanceUID)
            throws SeriesNotFoundException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasSeriesShareAccess(studyInstanceUID, seriesInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("The series does not exist or you don't have access")
                    .build();
            return Response.status(NOT_FOUND).entity(errorResponse).build();
        }

        try {
            Sending.shareSeriesWithUser(kheopsPrincipal.getUser(), username, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (UserNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response appropriateSeries(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                      @PathParam(SERIES_INSTANCE_UID) @UIDValidator String seriesInstanceUID)
            throws AlbumNotFoundException, SeriesNotFoundException, UserNotMemberException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (headerXAuthorizationSource != null) {
            final KheopsPrincipal tokenPrincipal;
            try {
                tokenPrincipal = getPrincipalFromHeadersXTokenSource(headerXAuthorizationSource);
            } catch (AccessTokenVerificationException e) {
                LOG.log(WARNING, "forbidden", e);
                return Response.status(FORBIDDEN).build();
            } catch (UserNotFoundException e) {
                return Response.status(FORBIDDEN).entity(e.getErrorResponse()).build();
            }

            if (!tokenPrincipal.hasSeriesShareAccess(studyInstanceUID, seriesInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(SERIES_NOT_FOUND)
                        .detail("The series does not exist or you don't have access")
                        .build();
                return Response.status(NOT_FOUND).entity(errorResponse).build();
            }

            if (!(tokenPrincipal instanceof CapabilityPrincipal)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(AUTHORIZATION_ERROR)
                        .detail("The token in the header 'X-Authorization-Source' is not a capability token")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }

            final Optional<Capability> optionalCapability = tokenPrincipal.getCapability();
            if (optionalCapability.isPresent()) {
                final Capability capability = optionalCapability.get();
                if (!capability.hasAppropriatePermission()) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(AUTHORIZATION_ERROR)
                            .detail("The token in the header 'X-Authorization-Source' as not the appropriate permission")
                            .build();
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }
            }
            try {
                if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                    final String albumID = kheopsPrincipal.getAlbumID();
                    if (kheopsPrincipal.hasAlbumPermission(ADD_SERIES, albumID)) {
                        Sending.putSeriesInAlbum(delayedWebhook, kheopsPrincipal, albumID, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
                        return Response.status(CREATED).build();
                    } else {
                        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                                .message(AUTHORIZATION_ERROR)
                                .detail("No write access with this credential")
                                .build();
                        return Response.status(FORBIDDEN).entity(errorResponse).build();
                    }
                } else {
                    Sending.appropriateSeriesFromToken(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
                    return Response.status(CREATED).build();
                }
            } catch (NotAlbumScopeTypeException | ClientIdNotFoundException e) {
                return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
            }
        } else {
            if (!kheopsPrincipal.hasSeriesAddAccess(studyInstanceUID, seriesInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(SERIES_NOT_FOUND)
                        .detail("The series does not exist or you don't have access")
                        .build();
                return Response.status(NOT_FOUND).entity(errorResponse).build();
            }
        }

        try {
            if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(ADD_SERIES, albumID)) {
                    Sending.putSeriesInAlbum(delayedWebhook, kheopsPrincipal, albumID, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(AUTHORIZATION_ERROR)
                            .detail("No write access with this credential")
                            .build();
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }
            } else {
                Sending.appropriateSeries(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
            }
        } catch (NotAlbumScopeTypeException | ClientIdNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }
        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = SEND_SERIES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response appropriateStudy(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                     @QueryParam(ALBUM) String albumId)
            throws AlbumNotFoundException, SeriesNotFoundException, StudyNotFoundException, UserNotMemberException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (headerXAuthorizationSource != null) {
            final KheopsPrincipal tokenPrincipal;
            try {
                tokenPrincipal = getPrincipalFromHeadersXTokenSource(headerXAuthorizationSource);
            } catch (AccessTokenVerificationException e) {
                LOG.log(WARNING, "verification error", e);
                return Response.status(FORBIDDEN).build();
            } catch (UserNotFoundException e) {
                LOG.log(WARNING, "user not found", e);
                return Response.status(FORBIDDEN).entity(e.getErrorResponse()).build();
            }

            if (!tokenPrincipal.hasStudyShareAccess(studyInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(STUDY_NOT_FOUND)
                        .detail("The study does not exist or you don't have access")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }

            if (!(tokenPrincipal instanceof CapabilityPrincipal)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(AUTHORIZATION_ERROR)
                    .detail("The token in the header 'X-Authorization-Source' is not a capability token")
                    .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }

            final Optional<Capability> optionalCapability = tokenPrincipal.getCapability();
            if (optionalCapability.isPresent()) {
                final Capability capability = optionalCapability.get();
                if (!capability.hasAppropriatePermission()) {
                    return Response.status(FORBIDDEN).build();
                }
                if (albumId != null) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("With the header 'X-Authorization-Source' the query parameter 'album' must not be set")
                            .build();
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
                albumId = capability.getAlbum().getId();
            }

        } else {
            if (!kheopsPrincipal.hasStudyShareAccess(studyInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(STUDY_NOT_FOUND)
                        .detail("The study does not exist or you don't have access with the appropriate permission")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }
        }

        try {
            if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(ADD_SERIES, albumID)) {
                    Sending.putStudyInAlbum(delayedWebhook, kheopsPrincipal, albumID, studyInstanceUID, albumId, false, kheopsPrincipal.getKheopsLogBuilder());
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(AUTHORIZATION_ERROR)
                            .detail("No write access with this credential")
                            .build();
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }
            } else {
                Sending.appropriateStudy(kheopsPrincipal.getUser(), studyInstanceUID, albumId, kheopsPrincipal.getKheopsLogBuilder());
            }
        } catch (NotAlbumScopeTypeException e) {
            LOG.log(WARNING, "Not found", e);
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }
        return Response.status(CREATED).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteStudyFromInbox(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                         @QueryParam("adminPassword") String adminPassword,
                                         @Context HttpServletRequest request,
                                         @Context HttpServletResponse response)
            throws AlbumNotFoundException, SeriesNotFoundException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                request.getRequestDispatcher("/studies/" + studyInstanceUID + "/albums/"+kheopsPrincipal.getAlbumID()).forward(request, response);
                return Response.status(response.getStatus()).entity(response.getOutputStream()).build();
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
            } catch (IOException | ServletException e) {
                LOG.log(WARNING, "Bad Request", e);
                return Response.status(BAD_REQUEST).build();
            }
        }

        if (!kheopsPrincipal.hasStudyViewAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("The study does not exist or you don't have")
                    .build();
            return Response.status(NOT_FOUND).entity(errorResponse).build();
        }

        if (!kheopsPrincipal.hasStudyDeleteAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(AUTHORIZATION_ERROR)
                    .detail("You don't have access with the delete permission")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        // Permanentely delete here
        if ( adminPassword != null) {
            LOG.log(WARNING, "Admin password != null");
            final String environmentAdminPassword = System.getenv("DICOMWEB_PROXY_SECRET");

            if (environmentAdminPassword.equals(adminPassword)) {
                LOG.log(WARNING, "Admin access in here");
            }
        } else {
            Sending.deleteStudyFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        }
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteSeriesFromInbox(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                          @PathParam(SERIES_INSTANCE_UID) @UIDValidator String seriesInstanceUID,
                                          @Context HttpServletRequest request,
                                          @Context HttpServletResponse response)
            throws AlbumNotFoundException, SeriesNotFoundException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                request.getRequestDispatcher("/studies/" + studyInstanceUID + "/series/" + seriesInstanceUID + "/albums/"+kheopsPrincipal.getAlbumID()).forward(request, response);
                return Response.status(response.getStatus()).entity(response.getOutputStream()).build();
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
            } catch (IOException | ServletException e) {
                LOG.log(WARNING, "Bad Request", e);
                return Response.status(BAD_REQUEST).build();
            }
        }

        if (!kheopsPrincipal.hasSeriesViewAccess(studyInstanceUID, seriesInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(SERIES_NOT_FOUND)
                    .detail("Series does not exist or you don't have access")
                    .build();
            return Response.status(NOT_FOUND).entity(errorResponse).build();
        }

        if (!kheopsPrincipal.hasSeriesDeleteAccess(studyInstanceUID, seriesInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(AUTHORIZATION_ERROR)
                    .detail("The token not allow you to delete a series")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        Sending.deleteSeriesFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = ADD_SERIES, context = PATH_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response putSeriesInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                     @PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                     @PathParam(SERIES_INSTANCE_UID) @UIDValidator String seriesInstanceUID)
            throws AlbumNotFoundException, UserNotMemberException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (headerXAuthorizationSource != null) {
            final KheopsPrincipal tokenPrincipal;
            try {
                tokenPrincipal = getPrincipalFromHeadersXTokenSource(headerXAuthorizationSource);
            } catch (AccessTokenVerificationException e) {
                LOG.log(WARNING, "verification error", e);
                return Response.status(FORBIDDEN).build();
            } catch (UserNotFoundException e) {
                LOG.log(WARNING, "user not found", e);
                return Response.status(FORBIDDEN).entity(e.getErrorResponse()).build();
            }

            if (!tokenPrincipal.hasSeriesShareAccess(studyInstanceUID, seriesInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(SERIES_NOT_FOUND)
                        .detail("The series does not exist or you don't have access")
                        .build();
                return Response.status(NOT_FOUND).entity(errorResponse).build();
            }

            if (!(tokenPrincipal instanceof CapabilityPrincipal)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(AUTHORIZATION_ERROR)
                        .detail("The token in the header 'X-Authorization-Source' is not a capability token")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }
            final Optional<Capability> optionalCapability = tokenPrincipal.getCapability();
            if (optionalCapability.isPresent()) {
                final Capability capability = optionalCapability.get();
                if (!capability.hasAppropriatePermission()) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(AUTHORIZATION_ERROR)
                            .detail("The token in the header 'X-Authorization-Source' as not the appropriate permission")
                            .build();
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }
            }

        } else {
            if (!kheopsPrincipal.hasSeriesAddAccess(studyInstanceUID, seriesInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(SERIES_NOT_FOUND)
                        .detail("The series does not exist or you don't have the send permission")
                        .build();
                return Response.status(NOT_FOUND).entity(errorResponse).build();
            }
        }

        try {
            Sending.putSeriesInAlbum(delayedWebhook, kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (ClientIdNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = SEND_SERIES, context = QUERY_PARAM)
    @AlbumPermissionSecured(permission = ADD_SERIES, context = PATH_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+ AlbumId.ID_PATTERN+"}")
    public Response putStudyInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                    @PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                    @QueryParam(ALBUM) String fromAlbumId,
                                    @QueryParam(INBOX) Boolean fromInbox)
            throws AlbumNotFoundException, StudyNotFoundException, UserNotMemberException {


        if (((fromAlbumId == null && fromInbox == null) ||
                (fromAlbumId != null && fromInbox != null && fromInbox)) && headerXAuthorizationSource == null) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Use 'album' xor 'inbox' query param")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        if(fromAlbumId != null) {
            fromInbox = false;
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (headerXAuthorizationSource != null) {
            final KheopsPrincipal tokenPrincipal;
            try {
                tokenPrincipal = getPrincipalFromHeadersXTokenSource(headerXAuthorizationSource);
            } catch (AccessTokenVerificationException e) {
                LOG.log(WARNING, "verification error", e);
                return Response.status(FORBIDDEN).build();
            } catch (UserNotFoundException e) {
                return Response.status(FORBIDDEN).entity(e.getErrorResponse()).build();
            }

            if (tokenPrincipal.getScope() == ScopeType.ALBUM)
            {
                try {
                    fromAlbumId = tokenPrincipal.getAlbumID();
                    if (!tokenPrincipal.hasAlbumPermission(SEND_SERIES, fromAlbumId)) {
                        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                                .message(AUTHORIZATION_ERROR)
                                .detail("The token not allow you to send a study")
                                .build();
                        return Response.status(FORBIDDEN).entity(errorResponse).build();
                    }
                    if (!tokenPrincipal.hasStudyShareAccess(studyInstanceUID)) {
                        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                                .message(STUDY_NOT_FOUND)
                                .detail("The study does not exist or you don't have access")
                                .build();
                        return Response.status(NOT_FOUND).entity(errorResponse).build();
                    }

                } catch (NotAlbumScopeTypeException e) {
                    return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
                }
            } else {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(AUTHORIZATION_ERROR)
                        .detail("The token in the header 'X-Authorization-Source' must be an album token")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }
        }

        try {
            Sending.putStudyInAlbum(delayedWebhook, kheopsPrincipal, albumId, studyInstanceUID, fromAlbumId, fromInbox, kheopsPrincipal.getKheopsLogBuilder());
        } catch (SeriesNotFoundException e) {
            LOG.log(WARNING, "not found", e);
            return Response.status(NOT_FOUND).build();
        }

        return Response.status(CREATED).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = DELETE_SERIES, context = PATH_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response deleteStudyFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                         @PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID)
            throws AlbumNotFoundException, SeriesNotFoundException, UserNotMemberException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        Sending.deleteStudyFromAlbum(context, kheopsPrincipal, albumId, studyInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = DELETE_SERIES, context = PATH_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response deleteSeriesFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                          @PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                          @PathParam(SERIES_INSTANCE_UID) @UIDValidator String seriesInstanceUID)
            throws AlbumNotFoundException, SeriesNotFoundException, UserNotMemberException {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        Sending.deleteSeriesFromAlbum(context, kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        return Response.status(NO_CONTENT).build();
    }

    private KheopsPrincipal getPrincipalFromHeadersXTokenSource(String token)
            throws AccessTokenVerificationException, UserNotFoundException {
        final AccessToken accessToken;
        try {
            accessToken = AccessTokenVerifier.authenticateAccessToken(context, getToken(token).getAccessToken());
        } catch (IllegalArgumentException e) {
            throw new NotAuthorizedException(Response.status(Response.Status.UNAUTHORIZED)
                    .header(WWW_AUTHENTICATE,"Basic").header(WWW_AUTHENTICATE,"Bearer").build());
        }
        final User user = getUser(accessToken.getSubject());

        return accessToken.newPrincipal(context, user);
    }
}
