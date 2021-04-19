package online.kheops.auth_server.token;

import online.kheops.auth_server.accesstoken.*;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.JweAesKey;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.lang.JoseException;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.accesstoken.AccessToken.TokenType.KEYCLOAK_TOKEN;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.INBOX;

class ViewerAccessTokenGenerator {
    private static final Logger LOG = Logger.getLogger(ViewerAccessTokenGenerator.class.getName());

    private final ServletContext servletContext;

    private String token;
    private String studyInstanceUID;
    private String sourceType;
    private String sourceId;
    private Collection<String> scopes;

    private ViewerAccessTokenGenerator(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    ViewerAccessTokenGenerator withToken(final String token) {
        this.token = Objects.requireNonNull(token);
        return this;
    }

    ViewerAccessTokenGenerator withStudyInstanceUID(final String studyInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID);
        return this;
    }

    ViewerAccessTokenGenerator withSourceType(final String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    ViewerAccessTokenGenerator withSourceId(final String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    ViewerAccessTokenGenerator withScopes(final Collection<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    static ViewerAccessTokenGenerator createGenerator(ServletContext servletContext) {
        return new ViewerAccessTokenGenerator(servletContext);
    }

    @SuppressWarnings("unchecked")
    String generate(@SuppressWarnings("SameParameterValue") final long expiresIn) {
        Objects.requireNonNull(token);
        Objects.requireNonNull(studyInstanceUID);

        if (sourceType != null && !sourceType.equals(ALBUM) && !sourceType.equals(INBOX)) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_REQUEST,
                    "'source_type' can be only '" + ALBUM + "' or '" + INBOX + "'");
        }
        if (sourceType != null && sourceType.equals(ALBUM) && (sourceId == null || sourceId.isEmpty())) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_REQUEST,
                    "'source_id' must be set when 'source_type'=" + ALBUM);
        }

        final AccessToken accessToken;
        try {
            accessToken = AccessTokenVerifier.authenticateAccessToken(servletContext, token);
        } catch (AccessTokenVerificationException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, e.getMessage(), e);
        } catch (DownloadKeyException e) {
            throw new WebApplicationException(e, Response.status(BAD_GATEWAY).entity("Error downloading the public key").build());
        }

        try {
            getUser(accessToken.getSubject());
        } catch (UserNotFoundException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "User not found", e);
        }

        if (accessToken.getTokenType() == AccessToken.TokenType.VIEWER_TOKEN ||
                accessToken.getTokenType() == AccessToken.TokenType.PEP_TOKEN) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "Request a viewer token is unauthorized with a viewer token");
        }

        try {
            final JSONObject data = new JSONObject();

            final String encodedToken;
            if (accessToken.getTokenType() == KEYCLOAK_TOKEN) {
                encodedToken = token.substring(token.indexOf('.') + 1, token.lastIndexOf('.'));
            } else {
                encodedToken = token;
            }

            data.put(Consts.JWE.TOKEN, encodedToken);
            if (sourceId != null) {
                data.put(Consts.JWE.SOURCE_ID, sourceId);
            }
            data.put(Consts.JWE.IS_INBOX, (sourceType != null && sourceType.equals(INBOX)));
            data.put(Consts.JWE.STUDY_INSTANCE_UID, studyInstanceUID);
            data.put(Consts.JWE.EXP, Date.from(Instant.now().plus(expiresIn, ChronoUnit.SECONDS)));
            if (scopes != null && scopes.size() > 0) {
                data.put(Consts.JWE.SCOPE, String.join(" ", scopes));
            }

            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setPayload(data.toJSONString());
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
            jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
            jwe.setKey(JweAesKey.getInstance().getKey());
            LOG.info(() -> "Returning viewer token for user: " + accessToken.getSubject() + "for studyInstanceUID " + studyInstanceUID);
            return jwe.getCompactSerialization();
        } catch (JoseException e) {
            LOG.log(Level.SEVERE, "JoseException", e);
            throw new WebApplicationException(e, Response.status(INTERNAL_SERVER_ERROR).entity("Error downloading the public key").build());
        }
    }

}
