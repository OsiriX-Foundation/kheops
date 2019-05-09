package online.kheops.auth_server.resource;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.annotation.ViewerTokenAccess;
import online.kheops.auth_server.assertion.*;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.Album;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.JweAesKey;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.lang.JoseException;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.INBOX;
import static online.kheops.auth_server.util.Tools.checkValidUID;

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

    static class IntreospectResponse {
    @XmlElement(name = "active")
    boolean active;
    @XmlElement(name = "scope")
    String scope;
    @XmlElement(name = "error")
    ErrorResponse error;
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

        errorResponse.error = "Bad Request";

        if(scope != null) {
            if (scope.equals("pep")) {
                if (studyInstanceUID == null || seriesInstanceUID == null) {
                    errorResponse.errorDescription = "With the scope: 'pep', 'study_instance_uid' and 'series_instance_uid' must be set";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
                if (!checkValidUID(studyInstanceUID)) {
                    errorResponse.errorDescription = "'study_instance_uid' is not a valid UID";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
                if (!checkValidUID(seriesInstanceUID)) {
                    errorResponse.errorDescription = "'series_instance_uid' is not a valid UID";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
            } else if (scope.equals("viewer")) {
                if (studyInstanceUID == null || sourceType == null) {
                    errorResponse.errorDescription = "With the scope: 'viewer', 'study_instance_uid' and 'source_type' must be set";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
                if (!checkValidUID(studyInstanceUID)) {
                    errorResponse.errorDescription = "'study_instance_uid' is not a valid UID";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
                if (!sourceType.equals(ALBUM) && !sourceType.equals(INBOX)) {
                    errorResponse.errorDescription = "'source_type' can be only '" + ALBUM + "' or '" + INBOX + "'";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
                if (sourceType.equals(ALBUM) && (sourceId.isEmpty() || sourceId == null)) {
                    errorResponse.errorDescription = "'source_id' must be set when 'source_type'=" + ALBUM;
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }

            } else if (scope.equals("report_provider_code")) {
                if (studyInstanceUID == null || studyInstanceUID.isEmpty()) {
                    errorResponse.errorDescription = "With the scope: 'report_provider_code', 'study_instance_uid' must be set";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
                if (!checkValidUID(studyInstanceUID)) {
                    errorResponse.errorDescription = "'study_instance_uid' is not a valid UID";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }

                if (sourceId.isEmpty() || sourceId == null) {
                    errorResponse.errorDescription = "'source_id' must be set with the album id ";
                    return Response.status(BAD_REQUEST).entity(errorResponse).build();
                }
            } else {
                errorResponse.errorDescription = "scope: must be 'pep' or 'viewer' not " + scope;
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
        } else {
            errorResponse.errorDescription = "scope: must be 'pep', 'viewer' or 'report_provider_code' not " + scope;
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }


        final String token;
        final long expiresIn;

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

        try {
            getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            LOG.log(Level.WARNING, "User not found", e);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (scope.equals("pep")) {

            if (assertion.getTokenType() == Assertion.TokenType.PEP_TOKEN ) {
                errorResponse.error = "Unauthorized";
                errorResponse.errorDescription = "Request a pep token is unauthorized with a pep token";
                return Response.status(UNAUTHORIZED).entity(errorResponse).build();
            }

            final User callingUser;
            try {
                callingUser = getOrCreateUser(assertion.getSub());
            } catch (UserNotFoundException e) {
                LOG.log(Level.WARNING, "User not found", e);
                errorResponse.errorDescription = "Unknown user";
                return Response.status(UNAUTHORIZED).entity(errorResponse).build();
            }

            try {
                final KheopsPrincipalInterface principal = assertion.newPrincipal(callingUser);
                if (!principal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                    throw new SeriesNotFoundException("");
                }
            } catch (SeriesNotFoundException e) {
                LOG.info("The user does not have access to the given StudyInstanceUID and SeriesInstanceUID pair");
                errorResponse.errorDescription = "The user does not have access to the given StudyInstanceUID and SeriesInstanceUID pair";
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            }
            // Generate a pep token
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
                    .withClaim("capability", false) // don't give capability access
                    .withClaim("study_uid", studyInstanceUID)
                    .withClaim("series_uid", seriesInstanceUID)
                    .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .withNotBefore(new Date());

            token = jwtBuilder.sign(algorithmHMAC);
            expiresIn = 3600L;

        } else if (scope.equals("viewer")) { //viewerScope
            // Generate a new Viewer token (JWE)

            if (assertion.getTokenType() == Assertion.TokenType.VIEWER_TOKEN ||
                    assertion.getTokenType() == Assertion.TokenType.PEP_TOKEN ) {
                errorResponse.error = "Unauthorized";
                errorResponse.errorDescription = "Request a viewer token is unauthorized with a pep token or a viewer token";
                return Response.status(UNAUTHORIZED).entity(errorResponse).build();
            }

            try {
                final JsonWebEncryption jwe = new JsonWebEncryption();

                JSONObject data = new JSONObject();
                data.put(Consts.JWE.TOKEN, assertionToken);
                data.put(Consts.JWE.SOURCE_ID, sourceId);
                data.put(Consts.JWE.IS_INBOX, sourceType.equals(INBOX));
                data.put(Consts.JWE.STUDY_INSTANCE_UID, studyInstanceUID);
                data.put(Consts.JWE.EXP, Date.from(Instant.now().plus(12, ChronoUnit.HOURS)));

                jwe.setPayload(data.toJSONString());
                jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
                jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
                jwe.setKey(JweAesKey.getInstance().getKey());
                String serializedJwe = jwe.getCompactSerialization();

                token = serializedJwe;
                expiresIn = 43200L;
            } catch (JoseException e) {
                LOG.log(Level.SEVERE, "JoseException", e);
                return Response.status(INTERNAL_SERVER_ERROR).entity(e.getStackTrace()).build();//TODO
            }
        } else if (scope.equals("report_provider_code")) {
            //vérifier la permission de créer report_provider_code (user) pas capability token
            if (assertion.getTokenType() == Assertion.TokenType.KEYCLOAK_TOKEN  ||
                    assertion.getTokenType() == Assertion.TokenType.SUPER_USER_TOKEN) {
                final User callingUser;
                try {
                    callingUser = getOrCreateUser(assertion.getSub());
                } catch (UserNotFoundException e) {
                    LOG.log(Level.WARNING, "User not found", e);
                    errorResponse.errorDescription = "Unknown user";
                    return Response.status(UNAUTHORIZED).entity(errorResponse).build();
                }
                //vérifier l'acces a l'album
                final KheopsPrincipalInterface principal = assertion.newPrincipal(callingUser);
                try {
                    if (principal.hasUserAccess() && principal.hasAlbumAccess(sourceId) ) {
                        final Album album = getAlbum(sourceId);
                        if (canAccessStudy(album, studyInstanceUID)) {
                            //TODO vérifier que la study est dans l'album

                            //générer le jwt
                            token = "token";
                            expiresIn = 120L; // 2minutes
                        } else {
                            return Response.status(FORBIDDEN).build();
                        }

                    } else {
                        return Response.status(FORBIDDEN).build();
                    }
                } catch (AlbumNotFoundException e) {
                    errorResponse.error = "Not found";
                    errorResponse.errorDescription = "Album : "+sourceId+" Not found";
                    return Response.status(FORBIDDEN).entity(errorResponse).build();
                }

            } else {
                return Response.status(FORBIDDEN).build();
            }

        } else {
            return Response.status(BAD_REQUEST).build();
        }

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.accessToken = token;
        tokenResponse.tokenType = "Bearer";
        tokenResponse.expiresIn = expiresIn;
        if (returnUser) {
            tokenResponse.user = assertion.getSub();
        }
        if(scope.equals("pep")) {
            LOG.info(() -> "Returning pep token for user: " + assertion.getSub() + "for studyInstanceUID " + studyInstanceUID +" seriesInstanceUID " + seriesInstanceUID);
        } else if (scope.equals("viewer")) { //viewerScope
            LOG.info(() ->"Returning viewer token for user: " + assertion.getSub() + "for studyInstanceUID " + studyInstanceUID);
        } else if (scope.equals("report_provider_code")) { //viewerScope
            LOG.info(() ->"Returning viewer token for user: " + assertion.getSub() + "for studyInstanceUID " + studyInstanceUID);
        }
        return Response.status(OK).entity(tokenResponse).build();
    }

    @POST
    @FormURLEncodedContentType
    @ViewerTokenAccess
    @Path("/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response introspect(@FormParam("grant_type") String grantType,
                               @FormParam("assertion") String assertionToken) {

        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.error = "invalid_grant";
        final IntreospectResponse intreospectResponse = new IntreospectResponse();

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
            intreospectResponse.error = errorResponse;
            intreospectResponse.active = false;
            return Response.status(OK).entity(intreospectResponse).build();
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            errorResponse.error = "server_error";
            errorResponse.errorDescription = "error";
            return Response.status(BAD_GATEWAY).entity(errorResponse).build();
        }


        final User callingUser;
        try {
            callingUser = getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            LOG.log(Level.WARNING, "user not found", e);
            errorResponse.error = "unknown_user";
            errorResponse.errorDescription = "The user was not found in the DB";
            intreospectResponse.error = errorResponse;
            intreospectResponse.active = false;
            return Response.status(OK).entity(intreospectResponse).build();
        }

        final Capability capability = assertion.getCapability().orElse(null);

        if(capability != null) {
            if (capability.getScopeType().equalsIgnoreCase(ScopeType.ALBUM.name())) {
                intreospectResponse.scope = (capability.isWritePermission()?"write ":"") +
                        (capability.isReadPermission()?"read ":"") +
                        (capability.isDownloadPermission()?"download ":"") +
                        (capability.isAppropriatePermission()?"appropriate ":"");
                if (intreospectResponse.scope.length() > 0) {
                    intreospectResponse.scope = intreospectResponse.scope.substring(0, intreospectResponse.scope.length() - 1);
                }
            } else {
                intreospectResponse.scope = "read write";
            }
        } else if(assertion.getViewer().isPresent()) {
            intreospectResponse.scope = "read";
        } else {
            intreospectResponse.scope = "read write";
        }

        intreospectResponse.active = true;
        return Response.status(OK).entity(intreospectResponse).build();
    }
}

