package online.kheops.auth_server.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

import javax.servlet.ServletContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class ReportProviderAuthCodeGenerator {
    private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

    private final ServletContext servletContext;

    private List<String> studyInstanceUIDs;
    private String clientId;
    private String subject;
    private String actingParty;
    private String capabilityTokenId;

    private ReportProviderAuthCodeGenerator(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public static ReportProviderAuthCodeGenerator createGenerator(final ServletContext servletContext) {
        return new ReportProviderAuthCodeGenerator(servletContext);
    }


    public ReportProviderAuthCodeGenerator withStudyInstanceUIDs(final List<String> studyInstanceUIDs) {
        this.studyInstanceUIDs = Objects.requireNonNull(studyInstanceUIDs);
        return this;
    }

    public ReportProviderAuthCodeGenerator withClientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    public ReportProviderAuthCodeGenerator withSubject(final String subject) {
        this.subject = Objects.requireNonNull(subject);
        return this;
    }

    public ReportProviderAuthCodeGenerator withActingParty(final String actingParty) {
        this.actingParty = Objects.requireNonNull(actingParty);
        return this;
    }

    public ReportProviderAuthCodeGenerator withCapabilityTokenId(final String capabilityTokenId) {
        this.capabilityTokenId = Objects.requireNonNull(capabilityTokenId);
        return this;
    }


    public String generate(@SuppressWarnings("SameParameterValue") long expiresIn) {

        final String authSecret = getHMACSecret();
        final Algorithm algorithmHMAC;
        try {
            algorithmHMAC = Algorithm.HMAC256(authSecret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("online.kheops.auth.hmacsecret is not a valid HMAC secret", e);
        }

        final JWTCreator.Builder jwtBuilder = JWT.create()
                .withExpiresAt(Date.from(Instant.now().plus(expiresIn, ChronoUnit.SECONDS)))
                .withNotBefore(new Date())
                .withArrayClaim("study_uids", studyInstanceUIDs.toArray(new String[0]))
                .withSubject(Objects.requireNonNull(subject))
                .withIssuer(getHostRoot())
                .withAudience(getHostRoot())
                .withClaim("azp", Objects.requireNonNull(clientId))
                .withClaim("type", "report_provider_code");

        if (actingParty != null) {
            jwtBuilder.withClaim("act", actingParty);
        }

        if (capabilityTokenId != null) {
            jwtBuilder.withClaim("cap_token", capabilityTokenId);
        }

        return jwtBuilder.sign(algorithmHMAC);
    }

    private String getHostRoot() {
        return Objects.requireNonNull(servletContext).getInitParameter(HOST_ROOT_PARAMETER);
    }

    private String getHMACSecret() {
        return Objects.requireNonNull(servletContext).getInitParameter(HMAC_SECRET_PARAMETER);
    }
}
