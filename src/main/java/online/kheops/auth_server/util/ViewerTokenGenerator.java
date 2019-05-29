package online.kheops.auth_server.util;

import online.kheops.auth_server.assertion.*;
import online.kheops.auth_server.user.UserNotFoundException;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.lang.JoseException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.Consts.INBOX;

public class ViewerTokenGenerator {
    private static final Logger LOG = Logger.getLogger(ViewerTokenGenerator.class.getName());

    private String token;
    private String studyInstanceUID;
    private String sourceType;
    private String sourceId;

    public ViewerTokenGenerator withToken(final String token) {
        this.token = Objects.requireNonNull(token);
        return this;
    }

    public ViewerTokenGenerator withStudyInstanceUID(final String studyInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID);
        return this;
    }

    public ViewerTokenGenerator withSourceType(final String sourceType) {
        this.sourceType = Objects.requireNonNull(sourceType);
        return this;
    }

    public ViewerTokenGenerator withSourceId(final String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public static ViewerTokenGenerator createGenerator() {
        return new ViewerTokenGenerator();
    }

    @SuppressWarnings("unchecked")
    public String generate(final long expiresIn) {
        Objects.requireNonNull(token);
        Objects.requireNonNull(studyInstanceUID);
        Objects.requireNonNull(sourceType);

        if (!sourceType.equals(ALBUM) && !sourceType.equals(INBOX)) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_REQUEST,
                    "'source_type' can be only '" + ALBUM + "' or '" + INBOX + "'");
        }
        if (sourceType.equals(ALBUM) && (sourceId == null || sourceId.isEmpty())) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_REQUEST,
                    "'source_id' must be set when 'source_type'=" + ALBUM);
        }

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(token);
        } catch (BadAssertionException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, e.getMessage(), e);
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            throw new WebApplicationException(e, Response.status(BAD_GATEWAY).entity("Error downloading the public key").build());
        }

        try {
            getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "User not found", e);
        }

        if (assertion.getTokenType() == Assertion.TokenType.VIEWER_TOKEN ||
                assertion.getTokenType() == Assertion.TokenType.PEP_TOKEN) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "Request a viewer token is unauthorized with a viewer token");
        }

        try {
            final JSONObject data = new JSONObject();
            data.put(Consts.JWE.TOKEN, token);
            data.put(Consts.JWE.SOURCE_ID, sourceId);
            data.put(Consts.JWE.IS_INBOX, sourceType.equals(INBOX));
            data.put(Consts.JWE.STUDY_INSTANCE_UID, studyInstanceUID);
            data.put(Consts.JWE.EXP, Date.from(Instant.now().plus(12, ChronoUnit.HOURS)));

            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setPayload(data.toJSONString());
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
            jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
            jwe.setKey(JweAesKey.getInstance().getKey());
            LOG.info(() -> "Returning viewer token for user: " + assertion.getSub() + "for studyInstanceUID " + studyInstanceUID);
            return jwe.getCompactSerialization();
        } catch (JoseException e) {
            LOG.log(Level.SEVERE, "JoseException", e);
            throw new WebApplicationException(e, Response.status(INTERNAL_SERVER_ERROR).entity("Error downloading the public key").build());
        }
    }

}
