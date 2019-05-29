package online.kheops.auth_server.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.assertion.*;
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

public class PepTokenGenerator {
    private static final Logger LOG = Logger.getLogger(PepTokenGenerator.class.getName());

    private final ServletContext context;

    private String token;
    private String studyInstanceUID;
    private String seriesInstanceUID;

    private PepTokenGenerator (final ServletContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public PepTokenGenerator withToken(final String token) {
        this.token = Objects.requireNonNull(token);
        return this;
    }

    public PepTokenGenerator withStudyInstanceUID(final String studyInstanceUID) {
        this.studyInstanceUID = Objects.requireNonNull(studyInstanceUID);
        return this;
    }

    public PepTokenGenerator withSeriesInstanceUID(final String seriesInstanceUID) {
        this.seriesInstanceUID = Objects.requireNonNull(seriesInstanceUID);
        return this;
    }

    public static PepTokenGenerator createGenerator(final ServletContext context) {
      return new PepTokenGenerator(context);
    }

    public String generate(long expiresIn) {

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(Objects.requireNonNull(token));
        } catch (BadAssertionException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, e.getMessage(), e);
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            throw new WebApplicationException(Response.status(BAD_GATEWAY).entity("Error downloading the public key").build());
        }

        if (assertion.getTokenType() == Assertion.TokenType.PEP_TOKEN ) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "Request a pep token is unauthorized with a pep token");
        }

        final User callingUser;
        try {
            callingUser = getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "User not found", e);
        }

        try {
            final KheopsPrincipalInterface principal = assertion.newPrincipal(callingUser);
            if (!principal.hasSeriesReadAccess(studyInstanceUID, seriesInstanceUID)) {
                throw new SeriesNotFoundException("");
            }
        } catch (SeriesNotFoundException e) {
            throw new TokenRequestException(TokenRequestException.Error.INVALID_GRANT, "The user does not have access to the given StudyInstanceUID and SeriesInstanceUID pair", e);
        }
        // Generate a pep token
        final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecret");
        final Algorithm algorithmHMAC;
        try {
            algorithmHMAC = Algorithm.HMAC256(authSecret);
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "online.kheops.auth.hmacsecret is not a valid HMAC secret", e);
            throw new WebApplicationException(Response.status(INTERNAL_SERVER_ERROR).entity("Error downloading the public key").build());
        }

        JWTCreator.Builder jwtBuilder = JWT.create()
                .withIssuer("auth.kheops.online")
                .withSubject(assertion.getSub())
                .withAudience("dicom.kheops.online")
                .withClaim("capability", false) // don't give capability access
                .withClaim("study_uid", studyInstanceUID)
                .withClaim("series_uid", seriesInstanceUID)
                .withExpiresAt(Date.from(Instant.now().plus(expiresIn, ChronoUnit.SECONDS)))
                .withNotBefore(new Date());

        LOG.info(() -> "Returning pep token for user: " + assertion.getSub() + "for studyInstanceUID " + studyInstanceUID +" seriesInstanceUID " + seriesInstanceUID);
        return jwtBuilder.sign(algorithmHMAC);
    }
}
