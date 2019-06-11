package online.kheops.auth_server.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.PACSAuthTokenBuilder;
import online.kheops.auth_server.accesstoken.*;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.user.Users.getOrCreateUser;

class PepTokenGenerator {
    private static final Logger LOG = Logger.getLogger(PepTokenGenerator.class.getName());

    private final ServletContext context;

    private String token;
    private String studyInstanceUID;
    private String seriesInstanceUID;

    private PepTokenGenerator (final ServletContext context) {
        this.context = Objects.requireNonNull(context);
    }

    PepTokenGenerator withToken(final String token) {
        this.token = Objects.requireNonNull(token);
        return this;
    }

    PepTokenGenerator withStudyInstanceUID(final String studyInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID);
        return this;
    }

    PepTokenGenerator withSeriesInstanceUID(final String seriesInstanceUID) {
        this.seriesInstanceUID = Objects.requireNonNull(seriesInstanceUID);
        return this;
    }

    static PepTokenGenerator createGenerator(final ServletContext context) {
      return new PepTokenGenerator(context);
    }

    String generate(long expiresIn) {

        final AccessToken accessToken;
        try {
            accessToken = AccessTokenVerifier.authenticateAccessToken(context, Objects.requireNonNull(token));
        } catch (AccessTokenVerificationException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, e.getMessage(), e);
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            throw new WebApplicationException(Response.status(BAD_GATEWAY).entity("Error downloading the public key").build());
        }

        if (accessToken.getTokenType() == AccessToken.TokenType.PEP_TOKEN ) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "Request a pep token is unauthorized with a pep token");
        }

        final User callingUser;
        try {
            callingUser = getOrCreateUser(accessToken.getSub());
        } catch (UserNotFoundException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "User not found", e);
        }

        try {
            final KheopsPrincipalInterface principal = accessToken.newPrincipal(context, callingUser);
            if (!principal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                throw new SeriesNotFoundException("");
            }
        } catch (SeriesNotFoundException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "The user does not have access to the given StudyInstanceUID and SeriesInstanceUID pair", e);
        }

        LOG.info(() -> "Returning pep token for user: " + accessToken.getSub() + "for studyInstanceUID " + studyInstanceUID +" seriesInstanceUID " + seriesInstanceUID);
        return PACSAuthTokenBuilder.newBuilder()
                .withStudyUID(studyInstanceUID)
                .withSeriesUID(seriesInstanceUID)
                .withSubject(accessToken.getSub())
                .build();
    }
}
