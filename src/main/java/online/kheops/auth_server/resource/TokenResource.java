package online.kheops.auth_server.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import online.kheops.auth_server.annotation.TokenSecurity;
import online.kheops.auth_server.accesstoken.*;
import online.kheops.auth_server.token.TokenClientKind;
import online.kheops.auth_server.token.TokenGrantType;
import online.kheops.auth_server.token.TokenRequestException;
import sun.tools.jstat.Token;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.Response.Status.*;
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

    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        Long expiresIn;
        @XmlElement(name = "user")
        String user;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class IntrospectResponse {
        @XmlElement(name = "active")
        boolean active;
        @XmlElement(name = "scope")
        String scope;
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
            LOG.log(WARNING, "error processing grant", e);
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

        if (securityContext.isUserInRole(TokenClientKind.PUBLIC.getRoleString())) {
            throw new NotAuthorizedException("Basic");
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

        if (securityContext.isUserInRole(TokenClientKind.REPORT_PROVIDER.getRoleString()) &&
                accessToken.getTokenType() != AccessToken.TokenType.REPORT_PROVIDER_TOKEN) {
            LOG.log(WARNING, "Report Provider introspecting a valid non-report provider token");
            return Response.status(OK).entity(errorIntrospectResponse).build();
        }

        introspectResponse.scope = accessToken.getScope().orElse(null);

        introspectResponse.active = true;
        return Response.status(OK).entity(introspectResponse).build();
    }
}

