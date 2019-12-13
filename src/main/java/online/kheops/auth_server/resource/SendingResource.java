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
import online.kheops.auth_server.util.ErrorResponse;

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
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.PATH_PARAM;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.QUERY_PARAM;
import static online.kheops.auth_server.filter.SecuredFilter.getToken;
import static online.kheops.auth_server.user.AlbumUserPermissions.*;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.HttpHeaders.X_AUTHORIZATION_SOURCE;

@Path("/")
public class SendingResource
{

    private static final Logger LOG = Logger.getLogger(SendingResource.class.getName());

    @Context
    private ServletContext context;

    @Context
    private SecurityContext securityContext;

    @HeaderParam(X_AUTHORIZATION_SOURCE)
    private String headerXAuthorizationSource;

    @PUT
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = SEND_SERIES, context = QUERY_PARAM)
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
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("Baq Query Parameter")
                            .detail("With an album capability token, 'inbox' and 'album' must not be set")
                            .build();
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                } else if (fromAlbumId == null) {
                    request.getRequestDispatcher("/studies/" + studyInstanceUID + "/users/" + username + "?&album=" + kheopsPrincipal.getAlbumID()).forward(request, response);
                    return Response.status(response.getStatus()).entity(response.getOutputStream()).build();
                }
            } catch (NotAlbumScopeTypeException | AlbumNotFoundException e) {
                return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
            } catch (IOException | ServletException e) {
                LOG.log(WARNING, "Bad Request", e);
                return Response.status(BAD_REQUEST).build();
            }
        }

        if ((fromAlbumId == null && fromInbox == null) ||
                (fromAlbumId != null && fromInbox != null && fromInbox)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Baq Query Parameter")
                    .detail("Use only '"+ALBUM+"' xor '"+INBOX+"' not both")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        if(fromAlbumId != null) {
            fromInbox = false;
        }

        if(fromInbox && !kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("Study not found in the inbox")
                    .build();
            return Response.status(NOT_FOUND).entity(errorResponse).build();
        }

        try {
            Sending.shareStudyWithUser(kheopsPrincipal.getUser(), username, studyInstanceUID, fromAlbumId, fromInbox, kheopsPrincipal.getKheopsLogBuilder());
        } catch (UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
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
                                        @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                        @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Series not found")
                    .detail("The series does not exist or you don't have access")
                    .build();
            return Response.status(NOT_FOUND).entity(errorResponse).build();
        }

        try {
            Sending.shareSeriesWithUser(kheopsPrincipal.getUser(), username, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        return Response.status(CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response appropriateSeries(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                      @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

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

            if (!tokenPrincipal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Series not found")
                        .detail("The series does not exist or you don't have access")
                        .build();
                return Response.status(NOT_FOUND).entity(errorResponse).build();
            }

            if (tokenPrincipal.getClass() != CapabilityPrincipal.class) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Forbidden")
                        .detail("The token in the header 'X-Authorization-Source' is not a capability token")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }

            final Optional<Capability> optionalCapability = tokenPrincipal.getCapability();
            if (optionalCapability.isPresent()) {
                final Capability capability = optionalCapability.get();
                if (!capability.hasAppropriatePermission()) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("Forbidden")
                            .detail("The token in the header 'X-Authorization-Source' as not the appropriate permission")
                            .build();
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }
            }

        } else {
            if (!kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Series not found")
                        .detail("The series does not exist or you don't have access")
                        .build();
                return Response.status(NOT_FOUND).entity(errorResponse).build();
            }
        }

        try {
            if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(ADD_SERIES, albumID)) {
                    Sending.putSeriesInAlbum(kheopsPrincipal, albumID, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("Forbidden")
                            .detail("No write access with this credential")
                            .build();
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }
            } else {
                Sending.appropriateSeries(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
            }
        } catch (AlbumNotFoundException | NotAlbumScopeTypeException | SeriesNotFoundException | ClientIdNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }
        return Response.status(CREATED).build();

    }

    @PUT
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = SEND_SERIES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response appropriateStudy(@PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                     @QueryParam(ALBUM) String albumId) {

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

            if (!tokenPrincipal.hasStudyReadAccess(studyInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Study not found")
                        .detail("The study does not exist or you don't have access")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }

            if (tokenPrincipal.getClass() != CapabilityPrincipal.class) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Forbidden")
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
                            .message("Bad Query Parameter")
                            .detail("With the header 'X-Authorization-Source' the query parameter 'album' must not be set")
                            .build();
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
                albumId = capability.getAlbum().getId();
            }

        } else {
            if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("Study not found")
                        .detail("The study does not exist or you don't have access with the appropriate permission")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }
        }

        try {
            if(kheopsPrincipal.getScope() == ScopeType.ALBUM) {
                final String albumID = kheopsPrincipal.getAlbumID();
                if (kheopsPrincipal.hasAlbumPermission(ADD_SERIES, albumID)) {
                    Sending.putStudyInAlbum(kheopsPrincipal, albumID, studyInstanceUID, albumId, false, kheopsPrincipal.getKheopsLogBuilder());
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("Authorization error")
                            .detail("No write access with this credential")
                            .build();
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }
            } else {
                Sending.appropriateStudy(kheopsPrincipal.getUser(), studyInstanceUID, albumId, kheopsPrincipal.getKheopsLogBuilder());
            }
        } catch (AlbumNotFoundException | NotAlbumScopeTypeException | SeriesNotFoundException e) {
            LOG.log(WARNING, "Not found", e);
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
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
                LOG.log(WARNING, "Bad request", e);
                return Response.status(BAD_REQUEST).build();
            }
        }

        if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Study not found")
                    .detail("The study does not exist or you don't have access with the delete permission")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                String albumId = kheopsPrincipal.getAlbumID();
                if(kheopsPrincipal.hasAlbumPermission(DELETE_SERIES,albumId)) {
                    Sending.deleteStudyFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
                    return Response.status(NO_CONTENT).build();
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("Authorization error")
                            .detail("The token not allow you to delete a study")
                            .build();
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
            } catch (AlbumNotFoundException | SeriesNotFoundException e) {
                return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
            }
        }

        try {
            Sending.deleteStudyFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
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
            } catch (NotAlbumScopeTypeException | AlbumNotFoundException e) {
                return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
            } catch (IOException | ServletException e) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message("error")
                        .detail(e.getMessage())
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
        }

        if (!kheopsPrincipal.hasStudyWriteAccess(studyInstanceUID) || !kheopsPrincipal.hasSeriesWriteAccess(studyInstanceUID, seriesInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Authorization error")
                    .detail("The token not allow you to delete a study")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        if (kheopsPrincipal.getScope() == ScopeType.ALBUM) {
            try {
                String albumId = kheopsPrincipal.getAlbumID();
                if(kheopsPrincipal.hasAlbumPermission(DELETE_SERIES,albumId)) {
                    Sending.deleteSeriesFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
                    return Response.status(NO_CONTENT).build();
                } else {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message("Authorization error")
                            .detail("The token not allow you to delete a study")
                            .build();
                    return Response.status(FORBIDDEN).build();
                }
            } catch (NotAlbumScopeTypeException e) {
                return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
            } catch (AlbumNotFoundException | SeriesNotFoundException e) {
                return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
            }
        }

        try {
            Sending.deleteSeriesFromInbox(kheopsPrincipal.getUser(), studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }
        return Response.status(NO_CONTENT).build();
    }

    @PUT
    @Secured
    @UserAccessSecured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = ADD_SERIES, context = PATH_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response putSeriesInAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                     @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                     @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

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
            LOG.log(WARNING, "Album not found", e);
            return Response.status(NOT_FOUND).build();
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
                                    @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                    @QueryParam(ALBUM) String fromAlbumId,
                                    @QueryParam(INBOX) Boolean fromInbox) {


        if (((fromAlbumId == null && fromInbox == null) ||
                (fromAlbumId != null && fromInbox != null && fromInbox)) && headerXAuthorizationSource == null) {
            return Response.status(BAD_REQUEST).entity("Use only {"+ALBUM+"} xor {"+INBOX+"}").build();
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
                LOG.log(WARNING, "user not found", e);
                return Response.status(FORBIDDEN).build();
            }

            if (tokenPrincipal.getScope() == ScopeType.ALBUM)
            {
                try {
                    fromAlbumId = tokenPrincipal.getAlbumID();
                    if (!tokenPrincipal.hasAlbumPermission(SEND_SERIES, fromAlbumId)) {
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

        }

        try {
            Sending.putStudyInAlbum(kheopsPrincipal, albumId, studyInstanceUID, fromAlbumId, fromInbox, kheopsPrincipal.getKheopsLogBuilder());
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
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
                                         @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        try {
            Sending.deleteStudyFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = DELETE_SERIES, context = PATH_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{"+ALBUM+":"+AlbumId.ID_PATTERN+"}")
    public Response deleteSeriesFromAlbum(@SuppressWarnings("RSReferenceInspection") @PathParam(ALBUM) String albumId,
                                          @PathParam(StudyInstanceUID) @UIDValidator String studyInstanceUID,
                                          @PathParam(SeriesInstanceUID) @UIDValidator String seriesInstanceUID) {

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        try {
            Sending.deleteSeriesFromAlbum(kheopsPrincipal, albumId, studyInstanceUID, seriesInstanceUID, kheopsPrincipal.getKheopsLogBuilder());
        } catch (AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(NOT_FOUND).entity(e.getErrorResponse()).build();
        }

        return Response.status(NO_CONTENT).build();
    }

    private KheopsPrincipal getPrincipalFromHeadersXTokenSource(String token)
            throws AccessTokenVerificationException, UserNotFoundException {

        final AccessToken accessToken = AccessTokenVerifier.authenticateAccessToken(context, getToken(token));
        final User user = getOrCreateUser(accessToken.getSubject());

        return accessToken.newPrincipal(context, user);
    }
}
