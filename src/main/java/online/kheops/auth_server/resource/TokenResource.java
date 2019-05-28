package online.kheops.auth_server.resource;


import com.fasterxml.jackson.annotation.JsonInclude;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.annotation.TokenSecurity;
import online.kheops.auth_server.assertion.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.util.*;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.util.TokenRequestException.Error.UNSUPPORTED_GRANT_TYPE;
import static online.kheops.auth_server.util.TokenRequestException.Error.INVALID_REQUEST;


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
    @FormURLEncodedContentType
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(MultivaluedMap<String, String> form) {
        final List<String> grantTypes = form.get("grant_type");

        if (grantTypes == null || form.get("grant_type").size() != 1) {
            LOG.log(WARNING, "Missing or duplicate grant_type");
            throw new TokenRequestException(INVALID_REQUEST, "Missing or duplicate grant_type");
        }

        final TokenGrantType grantType;
        try {
            grantType = TokenGrantType.fromString(form.getFirst("grant_type"));
        } catch (IllegalArgumentException e) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE, "Missing or duplicate grant_type");
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
    @FormURLEncodedContentType
    @Path("/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response introspect(@FormParam("token") String assertionToken) {

        final IntrospectResponse introspectResponse = new IntrospectResponse();

        if (!securityContext.isUserInRole(TokenClientKind.PUBLIC.getRoleString())) {
            throw new NotAuthorizedException("Basic");
        }
        // TODO secure this resource

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(assertionToken);
        } catch (BadAssertionException e) {
            LOG.log(WARNING, "Error validating a token", e);
            return Response.status(OK).entity(errorIntrospectResponse).build();
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            return Response.status(OK).entity(errorIntrospectResponse).build();
        }

        final Capability capability = assertion.getCapability().orElse(null);

        if(capability != null) {
            if (capability.getScopeType().equalsIgnoreCase(ScopeType.ALBUM.name())) {
                introspectResponse.scope = (capability.isWritePermission()?"write ":"") +
                        (capability.isReadPermission()?"read ":"") +
                        (capability.isDownloadPermission()?"download ":"") +
                        (capability.isAppropriatePermission()?"appropriate ":"");
                if (introspectResponse.scope.length() > 0) {
                    introspectResponse.scope = introspectResponse.scope.substring(0, introspectResponse.scope.length() - 1);
                }
            } else {
                introspectResponse.scope = "read write";
            }
        } else if(assertion.getViewer().isPresent()) {
            introspectResponse.scope = "read";
        } else {
            introspectResponse.scope = "read write";
        }

        introspectResponse.active = true;
        return Response.status(OK).entity(introspectResponse).build();
    }
}

