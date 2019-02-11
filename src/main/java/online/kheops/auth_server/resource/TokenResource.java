package online.kheops.auth_server.resource;


import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.annotation.ViewerTokenAccess;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;

import online.kheops.auth_server.assertion.BadAssertionException;
import online.kheops.auth_server.assertion.DownloadKeyException;
import online.kheops.auth_server.assertion.UnknownGrantTypeException;

import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.CapabilityPrincipal;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.principal.UserPrincipal;
import online.kheops.auth_server.principal.ViewerPrincipal;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.JweAesKey;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.lang.JoseException;

import java.io.UnsupportedEncodingException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.INBOX;

@Path("/")
public class TokenResource
{

    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    @Context
    ServletContext context;

    @Context
    ResourceContext resourceContext;

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

    static class ErrorResponse {
        @XmlElement(name = "error")
        String error;
        @XmlElement(name = "error_description")
        String errorDescription;
    }

    @POST
    @FormURLEncodedContentType
    @ViewerTokenAccess
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(@FormParam("grant_type") String grantType,
                          @FormParam("assertion") String assertionToken,
                          @FormParam("scope") String scope,
                          @FormParam("study_instance_uid") String studyInstanceUID,
                          @FormParam("series_instance_uid") String seriesInstanceUID,
                          @FormParam("source_type") String sourceType,
                          @FormParam("source_id") String sourceId,
                          @FormParam("return_user") @DefaultValue("false") boolean returnUser) {

        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.error = "invalid_grant";

        if (grantType == null) {
            errorResponse.errorDescription = "Missing grant_type";
            LOG.warning("Request for a token is missing a grant_type");
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }
        if (assertionToken == null) {
            errorResponse.errorDescription = "Missing assertion";
            LOG.warning("Request for a token is missing an assertion");
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(assertionToken, grantType);
        } catch (UnknownGrantTypeException e) {
            errorResponse.errorDescription = e.getMessage();
            LOG.log(Level.WARNING, "Unknown grant type", e);
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        } catch (BadAssertionException e) {
            errorResponse.errorDescription = e.getMessage();
            LOG.log(Level.WARNING, "Error validating a token", e);
            return Response.status(UNAUTHORIZED).entity(errorResponse).build();
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            errorResponse.error = "server_error";
            errorResponse.errorDescription = "error";
            return Response.status(BAD_GATEWAY).entity(errorResponse).build();
        }

        boolean pepScope = false;
        boolean viewerScope = false;

        if(scope != null && !scope.isEmpty()) {
            if(scope.compareTo("pep") == 0) {
                if(studyInstanceUID == null || seriesInstanceUID == null) {
                    errorResponse.errorDescription = "With the scope: 'pep', 'study_instance_uid' and 'series_instance_uid' must be set";
                    throw new WebApplicationException(Response.status(BAD_REQUEST).entity(errorResponse).build());
                }
                if (!checkValidUID(studyInstanceUID)) {
                    errorResponse.errorDescription = "'study_instance_uid' is not a valid UID";
                    throw new WebApplicationException(Response.status(BAD_REQUEST).entity(errorResponse).build());
                }
                if (!checkValidUID(seriesInstanceUID)) {
                    errorResponse.errorDescription = "'series_instance_uid' is not a valid UID";
                    throw new WebApplicationException(Response.status(BAD_REQUEST).entity(errorResponse).build());
                }
                pepScope = true;
            } else if(scope.compareTo("viewer") == 0) {
                if(studyInstanceUID == null || sourceType == null) {
                    errorResponse.errorDescription = "With the scope: 'viewer', 'study_instance_uid' and 'source_type' must be set";
                    throw new WebApplicationException(Response.status(BAD_REQUEST).entity(errorResponse).build());
                }
                if (!checkValidUID(studyInstanceUID)) {
                    errorResponse.errorDescription = "'study_instance_uid' is not a valid UID";
                    throw new WebApplicationException(Response.status(BAD_REQUEST).entity(errorResponse).build());
                }
                if (sourceType.compareTo(ALBUM)!=0 && sourceType.compareTo(INBOX) != 0 ) {
                    errorResponse.errorDescription = "'source_type' can be only '"+ALBUM+"' or '"+INBOX+"'";
                    throw new WebApplicationException(Response.status(BAD_REQUEST).entity(errorResponse).build());
                }
                if (sourceType.compareTo(ALBUM) == 0 && (sourceId.isEmpty() || sourceId == null) ) {
                    errorResponse.errorDescription = "'source_id' must be set when 'source_type'="+ALBUM;
                    throw new WebApplicationException(Response.status(BAD_REQUEST).entity(errorResponse).build());
                }
                viewerScope = true;
            } else {
                errorResponse.errorDescription = "Scope: can be only 'pep', 'viewer', null or Empty";
                throw new WebApplicationException(Response.status(BAD_REQUEST).entity(errorResponse).build());
            }
        }

        Response.Status responseStatus = OK;
        final String token;
        final long expiresIn;

        if (pepScope) {

            final User callingUser;
            try {
                callingUser = getOrCreateUser(assertion.getSub());
            } catch (UserNotFoundException e) {
                errorResponse.errorDescription = "Unknown user";
                return Response.status(UNAUTHORIZED).entity(errorResponse).build();
            }

            try {
                final KheopsPrincipalInterface principal;
                if(assertion.getCapability().isPresent()) {
                    principal = new CapabilityPrincipal(assertion.getCapability().get(), callingUser);
                } else if(assertion.getViewer().isPresent()) {
                    principal = new ViewerPrincipal(assertion.getViewer().get());
                } else {
                    principal = new UserPrincipal(callingUser);
                }
                if (!principal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                    throw new SeriesNotFoundException("");
                }
            } catch (SeriesNotFoundException e) {
                LOG.info("The user does not have access to the given StudyInstanceUID and SeriesInstanceUID pair");
                errorResponse.errorDescription = "The user does not have access to the given StudyInstanceUID and SeriesInstanceUID pair";
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }
        }

        if (viewerScope) {
            // Generate a new Viewer token (JWE)

            try {
                final JsonWebEncryption jwe = new JsonWebEncryption();

                JSONObject data = new JSONObject();
                data.put("token", assertionToken);
                data.put("sourceId", sourceId);
                data.put("isInbox", sourceType.compareTo(INBOX) == 0);
                data.put("studyInstanceUID", studyInstanceUID);
                data.put("exp", Date.from(Instant.now().plus(12, ChronoUnit.HOURS)));

                jwe.setPayload(data.toJSONString());
                jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
                jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
                jwe.setKey(JweAesKey.getInstance().getKey());
                String serializedJwe = jwe.getCompactSerialization();

                token = serializedJwe;
                expiresIn = 43200L;
            } catch (JoseException e) {
                e.printStackTrace();
                return Response.status(INTERNAL_SERVER_ERROR).entity(e.getStackTrace()).build();//TODO
            }

        } else {

            // Generate a new token (access token or pep token)
            final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecret");
            final Algorithm algorithmHMAC;
            try {
                algorithmHMAC = Algorithm.HMAC256(authSecret);
            } catch (UnsupportedEncodingException e) {
                LOG.log(Level.SEVERE, "online.kheops.auth.hmacsecret is not a valid HMAC secret", e);
                return Response.status(INTERNAL_SERVER_ERROR).build();
            }

            JWTCreator.Builder jwtBuilder = JWT.create()
                    .withIssuer("auth.kheops.online")
                    .withSubject(assertion.getSub())
                    .withAudience("dicom.kheops.online")
                    .withClaim("capability", assertion.hasCapabilityAccess()) // don't give capability access for capability assertions
                    .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .withNotBefore(new Date());

            if (pepScope) {
                jwtBuilder = jwtBuilder.withClaim("study_uid", studyInstanceUID)
                        .withClaim("series_uid", seriesInstanceUID);
            }

            token = jwtBuilder.sign(algorithmHMAC);
            expiresIn = 3600L;
        }

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.accessToken = token;
        tokenResponse.tokenType = "Bearer";
        tokenResponse.expiresIn = expiresIn;
        if (returnUser) {
            tokenResponse.user = assertion.getSub();
        }
        if(pepScope) {
            LOG.info("Returning pep token for user: " + assertion.getSub() + "for studyInstanceUID " + studyInstanceUID +" seriesInstanceUID " + seriesInstanceUID);
        } else if(viewerScope) {
            LOG.info("Returning viewer token for user: " + assertion.getSub() + "for studyInstanceUID " + studyInstanceUID);
        } else {
            LOG.info("Returning access token for user: " + assertion.getSub());
        }
        return Response.status(responseStatus).entity(tokenResponse).build();
    }

    private boolean checkValidUID(String uid) {
        try {
            new Oid(uid);
            return true;
        } catch (GSSException exception) {
            return false;
        }
    }
}

