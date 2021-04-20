package online.kheops.auth_server.resource;


import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.annotation.TokenSecurity;
import online.kheops.auth_server.accesstoken.*;
import online.kheops.auth_server.capability.CapabilityNotFoundException;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.report_provider.ReportProviderUriNotValidException;
import online.kheops.auth_server.token.*;
import online.kheops.auth_server.util.ErrorResponse;
import online.kheops.auth_server.util.KheopsLogBuilder;
import online.kheops.auth_server.util.KheopsLogBuilder.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.capability.Capabilities.getCapabilityWithID;
import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientId;
import static online.kheops.auth_server.report_provider.ReportProviders.getRedirectUri;
import static online.kheops.auth_server.token.TokenRequestException.Error.UNSUPPORTED_GRANT_TYPE;
import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_REQUEST;
import static online.kheops.auth_server.util.HttpHeaders.X_FORWARDED_FOR;


@Path("/")
public class TokenResource
{
    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    @Context
    private ServletContext context;

    @Context
    private TokenAuthenticationContext tokenAuthenticationContext;

    @Context
    private SecurityContext securityContext;

    @HeaderParam(X_FORWARDED_FOR)
    private String headerXForwardedFor;

    @POST
    @TokenSecurity
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(MultivaluedMap<String, String> form) {
        if (form == null) {
            throw new TokenRequestException(INVALID_REQUEST, "Missing form data");
        }

        final List<String> grantTypes = form.get("grant_type");

        if (grantTypes == null || grantTypes.size() != 1) {
            LOG.log(WARNING, "Missing or duplicate grant_type");
            throw new TokenRequestException(INVALID_REQUEST, "Missing or duplicate grant_type");
        }

        final TokenGrantType grantType;
        try {
            grantType = TokenGrantType.from(grantTypes.get(0));
        } catch (IllegalArgumentException e) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE, e);
        }

        try {
            final TokenGrantResult result = grantType.processGrant(securityContext, tokenAuthenticationContext, form);

            final KheopsLogBuilder logBuilder = new KheopsLogBuilder()
                    .user(result.getSubject())
                    .provenance(result.getTokenProvenance())
                    .clientID(securityContext.getUserPrincipal().getName())
                    .action(grantType.getLogActionType())
                    .link(false);
            result.getScope().ifPresent(logBuilder::scope);
            result.getStudyInstanceUID().ifPresent(logBuilder::study);
            result.getSeriesInstanceUID().ifPresent(logBuilder::series);
            if (headerXForwardedFor != null) {
                logBuilder.ip(headerXForwardedFor);
            }
            logBuilder.log();

            return Response.ok(result.getTokenResponseEntity()).build();
        } catch (WebApplicationException e) {
            LOG.log(WARNING, "error processing grant", e); //NOSONAR
            throw e; //NOSONAR
        }
    }

    @POST
    @TokenSecurity
    @Path("/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response introspect(@FormParam("token") String assertionToken) {

        if (assertionToken == null) {
            LOG.log(WARNING, "Missing token");
            return Response.status(OK).entity(IntrospectResponse.getInactiveResponseJson()).build();
        }

        final AccessToken accessToken;
        try {
            accessToken = AccessTokenVerifier.authenticateIntrospectableAccessToken(tokenAuthenticationContext, assertionToken);
        } catch (AccessTokenVerificationException e) {
            LOG.log(WARNING, "Error validating a token", e);
            return Response.status(OK).entity(IntrospectResponse.getInactiveResponseJson()).build();
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            return Response.status(OK).entity(IntrospectResponse.getInactiveResponseJson()).build();
        }

        if (accessToken.getTokenType() == AccessToken.TokenType.REPORT_PROVIDER_TOKEN) {
            final String clientId = accessToken.getClientId().orElseThrow(InternalServerErrorException::new);

            final EntityManager em = EntityManagerListener.createEntityManager();
            final EntityTransaction tx = em.getTransaction();

            final ReportProvider reportProvider;
            final String albumId;
            final String redirectUri;
            try {
                tx.begin();
                reportProvider = getReportProviderWithClientId(clientId, em);
                albumId = reportProvider.getAlbum().getId();
                redirectUri = getRedirectUri(reportProvider);
            } catch (ReportProviderUriNotValidException e) {
                LOG.log(WARNING, "Unable to get the Report Provider's redirect_uri", e);
                return Response.status(OK).entity(IntrospectResponse.getInactiveResponseJson()).build();
            } catch (NoResultException e) {
                LOG.log(WARNING, "ClientId: "+ clientId + " Not Found", e);
                return Response.status(OK).entity(IntrospectResponse.getInactiveResponseJson()).build();
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }

            final KheopsLogBuilder logBuilder = new KheopsLogBuilder().user(accessToken.getSubject())
                    .provenance(accessToken)
                    .clientID(securityContext.getUserPrincipal().getName())
                    .action(ActionType.INTROSPECT_TOKEN)
                    .link(false);
            accessToken.getCapabilityTokenId().ifPresent(logBuilder::capabilityID);
            if (headerXForwardedFor != null) {
                logBuilder.ip(headerXForwardedFor);
            }
            logBuilder.log();

            final IntrospectResponse introspectResponse = IntrospectResponse.from(accessToken);
            introspectResponse.setAlbumId(albumId);
            introspectResponse.setRedirectUri(redirectUri);
            return Response.status(OK).entity(introspectResponse.toJson()).build();
        } else if (securityContext.isUserInRole(TokenClientKind.INTERNAL.getRoleString()) ||
                accessToken.getTokenType() == AccessToken.TokenType.ALBUM_CAPABILITY_TOKEN ||
                accessToken.getTokenType() == AccessToken.TokenType.USER_CAPABILITY_TOKEN) {
            final KheopsLogBuilder logBuilder = new KheopsLogBuilder().user(accessToken.getSubject())
                    .provenance(accessToken)
                    .clientID(securityContext.getUserPrincipal().getName())
                    .action(ActionType.INTROSPECT_TOKEN)
                    .link(false);
            final IntrospectResponse introspectResponse = IntrospectResponse.from(accessToken);
            if (accessToken.getTokenType() == AccessToken.TokenType.ALBUM_CAPABILITY_TOKEN) {
                final Capability capability;
                try {
                    if(accessToken.getCapabilityTokenId().isPresent()) {
                        capability = getCapabilityWithID(accessToken.getCapabilityTokenId().get());
                    } else {
                        final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                                .message("Not Found")
                                .detail("Capability token not found")
                                .build();
                        throw new CapabilityNotFoundException(errorResponse);
                    }
                } catch (CapabilityNotFoundException e) {
                    return Response.status(BAD_REQUEST).entity(e.getErrorResponse()).build();
                }

                introspectResponse.setAlbumId(capability.getAlbum().getId());
            }
            if (headerXForwardedFor != null) {
                logBuilder.ip(headerXForwardedFor);
            }
            logBuilder.log();

            return Response.status(OK).entity(introspectResponse.toJson()).build();
        } else {
            LOG.log(WARNING, "Public or Report Provider attempting to introspect a valid non-report provider token");
            return Response.status(UNAUTHORIZED).build();
        }
    }
}

