package online.kheops.auth_server.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.util.JWTs;
import online.kheops.auth_server.util.Source;

import javax.servlet.ServletContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class ReportProviderLoginHintGenerator {
    private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

    private final ServletContext servletContext;

    private List<String> studyInstanceUIDs;
    private Source source;
    private String clientId;
    private String subject;
    private String email;
    private String actingParty;
    private String capabilityTokenId;
    private Boolean oidcInitiated;

    private ReportProviderLoginHintGenerator(final ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public static ReportProviderLoginHintGenerator createGenerator(final ServletContext servletContext) {
        return new ReportProviderLoginHintGenerator(servletContext);
    }

    public ReportProviderLoginHintGenerator withStudyInstanceUIDs(final List<String> studyInstanceUIDs) {
        this.studyInstanceUIDs = Objects.requireNonNull(studyInstanceUIDs);
        return this;
    }

    public ReportProviderLoginHintGenerator withSource(final Source source) {
        this.source = Objects.requireNonNull(source);
        return this;
    }

    public ReportProviderLoginHintGenerator withClientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    public ReportProviderLoginHintGenerator withSubject(final String subject) {
        this.subject = Objects.requireNonNull(subject);
        return this;
    }

    public ReportProviderLoginHintGenerator withEmail(final String email) {
        this.email = email;
        return this;
    }

    public ReportProviderLoginHintGenerator withActingParty(final String actingParty) {
        this.actingParty = actingParty;
        return this;
    }

    public ReportProviderLoginHintGenerator withCapabilityTokenId(final String capabilityTokenId) {
        this.capabilityTokenId = capabilityTokenId;
        return this;
    }

    public ReportProviderLoginHintGenerator withOidcInitiated(final Boolean oidcInitiated) {
        this.oidcInitiated = oidcInitiated;
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

        JWTs.encodeSource(jwtBuilder, source);

        if (actingParty != null) {
            jwtBuilder.withClaim("act", actingParty);
        }

        if (email != null) {
            jwtBuilder.withClaim("email", email);
        }

        if (capabilityTokenId != null) {
            jwtBuilder.withClaim("cap_token", capabilityTokenId);
        }

        if (oidcInitiated != null) {
            jwtBuilder.withClaim("oidcInitiated", oidcInitiated);
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
