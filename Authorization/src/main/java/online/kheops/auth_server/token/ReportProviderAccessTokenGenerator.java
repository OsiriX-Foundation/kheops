package online.kheops.auth_server.token;

import online.kheops.auth_server.util.JWTs;
import online.kheops.auth_server.util.Source;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import javax.ws.rs.InternalServerErrorException;
import java.util.*;

import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class ReportProviderAccessTokenGenerator {
    private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

    private final TokenAuthenticationContext context;
    private String subject;
    private String actingParty;
    private String capabilityTokenId;
    private String clientId;
    private String scope;
    private Set<String> studyInstanceUIDs;
    private Source source;

    public static ReportProviderAccessTokenGenerator createGenerator(final TokenAuthenticationContext tokenAuthenticationContext) {
        return new ReportProviderAccessTokenGenerator(tokenAuthenticationContext);
    }

    public ReportProviderAccessTokenGenerator withSubject(final String subject) {
        this.subject = Objects.requireNonNull(subject);
        return this;
    }

    public ReportProviderAccessTokenGenerator withScope(final String scope) {
        this.scope = Objects.requireNonNull(scope);
        return this;
    }

    public ReportProviderAccessTokenGenerator withClientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ReportProviderAccessTokenGenerator withActingParty(final String actingParty) {
        this.actingParty = actingParty;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ReportProviderAccessTokenGenerator withCapabilityTokenId(final String capabilityTokenId) {
        this.capabilityTokenId = capabilityTokenId;
        return this;
    }

    public ReportProviderAccessTokenGenerator withStudyInstanceUIDs(final Collection<String> studyInstanceUIDs) {
        this.studyInstanceUIDs = new HashSet<>(studyInstanceUIDs);
        return this;
    }

    public ReportProviderAccessTokenGenerator withSource(Source source) {
        this.source = source;
        return this;
    }

    public String generate(@SuppressWarnings("SameParameterValue") long expiresIn) {

        JwtClaims claims = new JwtClaims();
        claims.setIssuer(getIssuerHost());
        claims.setSubject(Objects.requireNonNull(subject));
        claims.setAudience(getAudienceHost());
        claims.setExpirationTimeMinutesInTheFuture(expiresIn / 60f);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(0);
        claims.setClaim("azp", Objects.requireNonNull(clientId));
        claims.setClaim("scope", Objects.requireNonNull(scope));
        claims.setClaim("type", "report_generator");
        claims.setClaim("studyUID", studyInstanceUIDs.toArray(new String[0]));
        JWTs.encodeSource(claims, source);

        if (actingParty != null) {
            claims.setClaim("act", Collections.singletonMap("sub", actingParty));
        }

        if (capabilityTokenId != null) {
            claims.setClaim("cap_token", capabilityTokenId);
        }

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setKey(new HmacKey(getHMAC256Secret().getBytes()));

        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new InternalServerErrorException("Error signing the token", e);
        }
    }

    private ReportProviderAccessTokenGenerator(final TokenAuthenticationContext tokenAuthenticationContext) {
        this.context = tokenAuthenticationContext;
    }

    private String getAudienceHost() {
        return context.getServletContext().getInitParameter(HOST_ROOT_PARAMETER);
    }

    private String getIssuerHost() {
        return context.getServletContext().getInitParameter(HOST_ROOT_PARAMETER);
    }

    private String getHMAC256Secret() {
        return context.getServletContext().getInitParameter(HMAC_SECRET_PARAMETER);
    }
}
