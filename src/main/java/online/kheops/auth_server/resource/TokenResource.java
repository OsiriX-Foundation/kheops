package online.kheops.auth_server.resource;


import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.annotation.TokenSecurity;
import online.kheops.auth_server.accesstoken.*;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.report_provider.ReportProviderUriNotValidException;
import online.kheops.auth_server.token.TokenClientKind;
import online.kheops.auth_server.token.TokenGrantType;
import online.kheops.auth_server.token.TokenRequestException;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientId;
import static online.kheops.auth_server.report_provider.ReportProviders.getRedirectUri;
import static online.kheops.auth_server.token.TokenRequestException.Error.UNSUPPORTED_GRANT_TYPE;
import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_REQUEST;


@Path("/")
public class TokenResource
{
    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    @Context
    ServletContext context;

    @Context
    SecurityContext securityContext;

    static class IntrospectResponse {
        @XmlElement(name = "active")
        boolean active;
        @XmlElement(name = "scope")
        String scope;
        @XmlElement(name = "redirect_uri")
        String redirectUri;
        @XmlElement(name = "client_id")
        String clientId;
        @XmlElement(name = "album_id")
        String albumId;
    }

    private IntrospectResponse errorIntrospectResponse = new IntrospectResponse();

    @POST
    @TokenSecurity
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(MultivaluedMap<String, String> form) {
        final List<String> grantTypes = form.get("grant_type");

        if (grantTypes == null || grantTypes.size() != 1) {
            LOG.log(WARNING, "Missing or duplicate grant_type");
            throw new TokenRequestException(INVALID_REQUEST, "Missing or duplicate grant_type");
        }

        final TokenGrantType grantType;
        try {
            grantType = TokenGrantType.fromString(grantTypes.get(0));
        } catch (IllegalArgumentException e) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }

        try {
            return grantType.processGrant(securityContext, context, form);
        } catch (WebApplicationException e) {
            LOG.log(WARNING, "error processing grant", e); //NOSONAR
            throw e;
        }
    }

    @POST
    @TokenSecurity
    @Path("/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response introspect(@FormParam("token") String assertionToken) {

        final IntrospectResponse introspectResponse = new IntrospectResponse();

        if (assertionToken == null) {
            LOG.log(WARNING, "Missing token");
            return Response.status(OK).entity(errorIntrospectResponse).build();
        }

        final AccessToken accessToken;
        try {
            accessToken = AccessTokenVerifier.authenticateIntrospectableAccessToken(context, assertionToken);
        } catch (AccessTokenVerificationException e) {
            LOG.log(WARNING, "Error validating a token", e);
            return Response.status(OK).entity(errorIntrospectResponse).build();
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            return Response.status(OK).entity(errorIntrospectResponse).build();
        }

        if (accessToken.getTokenType() == AccessToken.TokenType.REPORT_PROVIDER_TOKEN) {
            final String clientId = ((ReportProviderAccessToken) accessToken).getClientId();

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
                return Response.status(OK).entity(errorIntrospectResponse).build();
            } catch (NoResultException e){
                LOG.log(WARNING, "ClientId: "+ clientId + " Not Found", e);
                return Response.status(OK).entity(errorIntrospectResponse).build();
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }

            introspectResponse.scope = accessToken.getScope().orElse(null);
            introspectResponse.clientId = clientId;
            introspectResponse.albumId = albumId;
            introspectResponse.redirectUri = redirectUri;
            introspectResponse.active = true;
            return Response.status(OK).entity(introspectResponse).build();
        }

        if (securityContext.isUserInRole(TokenClientKind.INTERNAL.getRoleString())) {
            introspectResponse.scope = accessToken.getScope().orElse(null);
            introspectResponse.active = true;
            return Response.status(OK).entity(introspectResponse).build();
        } else {
            LOG.log(WARNING, "Public or Report Provider attempting to introspect a valid non-report provider token");
            return Response.status(OK).entity(errorIntrospectResponse).build();
        }
    }
}

