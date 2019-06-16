package online.kheops.auth_server.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import javax.servlet.ServletContext;
import javax.ws.rs.InternalServerErrorException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

class ReportProviderAccessTokenGenerator {
    private static final String HOST_ROOT_PARAMETER = "online.kheops.root.uri";
    private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

    private final ServletContext context;
    private String subject;
    private Date authTime;
    private String clientId;
    private String scope;
    private Set<String> studyInstanceUIDs;

    static ReportProviderAccessTokenGenerator createGenerator(final ServletContext servletContext) {
        return new ReportProviderAccessTokenGenerator(servletContext);
    }

    ReportProviderAccessTokenGenerator withSubject(final String subject) {
        this.subject = Objects.requireNonNull(subject);
        return this;
    }

    @SuppressWarnings("unused")
    ReportProviderAccessTokenGenerator withAuthTime(final Date authTime) {
        this.authTime = authTime;
        return this;
    }

    ReportProviderAccessTokenGenerator withScope(final String scope) {
        this.scope = Objects.requireNonNull(scope);
        return this;
    }

    ReportProviderAccessTokenGenerator withClientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    ReportProviderAccessTokenGenerator withStudyInstanceUIDs(final Collection<String> studyInstanceUIDs) {
        this.studyInstanceUIDs = new HashSet<>(studyInstanceUIDs);
        return this;
    }

    String generate(@SuppressWarnings("SameParameterValue") long expiresIn) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(getHMAC256Secret());
            return JWT.create()
                    .withIssuer(getIssuerHost())
                    .withSubject(Objects.requireNonNull(subject))
                    .withAudience(getAudienceHost())
                    .withExpiresAt(Date.from(Instant.now().plus(expiresIn, ChronoUnit.SECONDS)))
                    .withIssuedAt(Date.from(Instant.now()))
                    .withNotBefore(new Date())
                    .withClaim("auth_time", authTime != null ? authTime : Date.from(Instant.now()))
                    .withClaim("azp", Objects.requireNonNull(clientId))
                    .withClaim("scope", Objects.requireNonNull(scope))
                    .withClaim("type", "report_generator")
                    .withArrayClaim("study_uids", studyInstanceUIDs.toArray(new String[0]))
                    .sign(algorithm);
        } catch (JWTCreationException | UnsupportedEncodingException e) {
            throw new InternalServerErrorException("Error signing the token", e);
        }
    }

    private ReportProviderAccessTokenGenerator(final ServletContext servletContext) {
        this.context = servletContext;
    }

    private String getAudienceHost() {
        return context.getInitParameter(HOST_ROOT_PARAMETER);
    }

    private String getIssuerHost() {
        return context.getInitParameter(HOST_ROOT_PARAMETER);
    }

    private String getHMAC256Secret() {
        return context.getInitParameter(HMAC_SECRET_PARAMETER);
    }
}
