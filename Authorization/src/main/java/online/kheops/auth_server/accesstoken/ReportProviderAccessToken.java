package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.*;
import online.kheops.auth_server.token.TokenAuthenticationContext;
import online.kheops.auth_server.util.JWTs;
import online.kheops.auth_server.util.Source;

import java.time.Instant;
import java.util.*;

import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class ReportProviderAccessToken implements AccessToken {
    private final String sub;
    private final List<String> studyUIDs;
    private final Source source;
    private final String clientId;
    private final String actingParty;
    private final String capabilityTokenId;
    private final boolean hasReadAccess;
    private final boolean hasWriteAccess;
    private final Instant exp;
    private final Instant iat;
    private final Instant nbf;
    private final List<String> aud;
    private final String iss;
    private final String token;

    static class Builder implements AccessTokenBuilder{

        private final TokenAuthenticationContext tokenAuthenticationContext;

        Builder(TokenAuthenticationContext tokenAuthenticationContext) {
            this.tokenAuthenticationContext = Objects.requireNonNull(tokenAuthenticationContext);
        }

        public ReportProviderAccessToken build(String assertionToken, boolean verifySignature) throws AccessTokenVerificationException {
            Objects.requireNonNull(assertionToken);

            final Algorithm algorithm;
            try {
                algorithm = Algorithm.HMAC256(authorizationSecret());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("authorizationSecret is not a valid HMAC256 secret", e);
            }
            final DecodedJWT jwt;
            try {
                jwt = JWT.require(algorithm)
                        .withIssuer(getIssuerHost())
                        .withAudience(getIssuerHost())
                        .withClaim("type", "report_generator")
                        .acceptLeeway(60)
                        .build()
                        .verify(assertionToken);
            } catch (JWTVerificationException e) {
                throw new AccessTokenVerificationException("AccessToken verification failed.", e);
            }

            if (jwt.getSubject() == null) {
                throw new AccessTokenVerificationException("Missing sub claim in token.");
            }

            final Claim azpClaim = jwt.getClaim("azp");
            if (azpClaim.isNull() || azpClaim.asString() == null) {
                throw new AccessTokenVerificationException("Missing azp claim in token");
            }

            final Claim scopeClaim = jwt.getClaim("scope");
            if (scopeClaim.isNull() || scopeClaim.asString() == null) {
                throw new AccessTokenVerificationException("Missing scope claim in token");
            }

            try {
                final boolean hasReadAccess = AccessTokenUtils.StringContainsScope(scopeClaim.asString(), "read");
                final boolean hasWriteAccess = AccessTokenUtils.StringContainsScope(scopeClaim.asString(), "write");
                final Instant exp = jwt.getExpiresAt().toInstant();
                final Instant iat = jwt.getIssuedAt().toInstant();
                final Instant nbf = jwt.getNotBefore().toInstant();
                final List<String> aud = Objects.requireNonNull(jwt.getAudience());
                final String iss = Objects.requireNonNull(jwt.getIssuer());
                final List<String> studyUIDs = jwt.getClaim("studyUID").asList(String.class);
                final Source source = JWTs.decodeSource(jwt);

                final String actingParty;
                Claim actClaim = jwt.getClaim("act");
                if (!actClaim.isNull()) {
                    actingParty = (String) actClaim.asMap().get("sub");
                    if (actingParty == null) {
                        throw new AccessTokenVerificationException("Has acting party, but without a subject");
                    }
                } else {
                    actingParty = null;
                }

                final String capabilityTokenId;
                Claim capabilityTokenIdClaim = jwt.getClaim("cap_token");
                if (!capabilityTokenIdClaim.isNull()) {
                    capabilityTokenId = capabilityTokenIdClaim.asString();
                } else {
                    capabilityTokenId = null;
                }

                return new ReportProviderAccessToken(jwt.getSubject(), actingParty, capabilityTokenId, studyUIDs, source, azpClaim.asString(), hasReadAccess, hasWriteAccess, exp, iat, nbf, aud, iss, assertionToken);
            } catch (NullPointerException | JWTDecodeException | IllegalArgumentException e) {
                throw new AccessTokenVerificationException("AccessToken missing fields.", e);
            } catch (ClassCastException e) {
                throw new AccessTokenVerificationException("Unable to read the acting party", e);
            }
        }

        private String authorizationSecret() {
            return tokenAuthenticationContext.getServletContext().getInitParameter("online.kheops.auth.hmacsecret");
        }

        private String getIssuerHost() {
            return tokenAuthenticationContext.getServletContext().getInitParameter(HOST_ROOT_PARAMETER);
        }

    }

    private ReportProviderAccessToken(String sub, String actingParty, String capabilityTokenId, List<String> studyUIDs, Source source, String clientId, boolean hasReadAccess, boolean hasWriteAccess,
                                      Instant exp, Instant iat, Instant nbf, List<String> aud, String iss, String token) {
        this.sub = Objects.requireNonNull(sub);
        this.actingParty = actingParty;
        this.capabilityTokenId = capabilityTokenId;
        this.studyUIDs = Objects.requireNonNull(studyUIDs);
        this.source = Objects.requireNonNull(source);
        this.clientId = Objects.requireNonNull(clientId);
        this.hasReadAccess = hasReadAccess;
        this.hasWriteAccess = hasWriteAccess;
        this.exp = exp;
        this.iat = iat;
        this.nbf = nbf;
        this.aud = aud;
        this.iss = iss;
        this.token = token;
    }

    public Source getSource() {
        return source;
    }

    @Override
    public String getSubject() {
        return sub;
    }

    @Override
    public TokenType getTokenType() { return TokenType.REPORT_PROVIDER_TOKEN; }

    @Override
    public Optional<String> getScope() {
        final List<String> scopes = new ArrayList<>(2);

        if (hasReadAccess) {
            scopes.add("read");
        }
        if (hasWriteAccess) {
            scopes.add("write");
        }

        if (!scopes.isEmpty()) {
            return Optional.of(String.join(" ", scopes));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getClientId() {
        return Optional.of(clientId);
    }

    @Override
    public Optional<List<String>> getStudyUIDs() {
        return Optional.of(studyUIDs);
    }

    @Override
    public Optional<Instant> getExpiresAt() {
        return Optional.of(exp);
    }

    @Override
    public Optional<Instant> getIssuedAt() {
        return Optional.of(iat);
    }

    @Override
    public Optional<Instant> getNotBefore() {
        return Optional.of(nbf);
    }

    @Override
    public Optional<List<String>> getAudience() {
        return Optional.of(aud);
    }

    @Override
    public Optional<String> getIssuer() {
        return Optional.of(iss);
    }

    @Override
    public Optional<String> getAuthorizedParty() {
        return Optional.of(clientId);
    }

    @Override
    public Optional<String> getActingParty() {
        return Optional.ofNullable(actingParty);
    }

    @Override
    public Optional<String> getCapabilityTokenId() {
        return Optional.ofNullable(capabilityTokenId);
    }

    @Override
    public KheopsPrincipal newPrincipal(TokenAuthenticationContext tokenAuthenticationContext, User user) {
        return new ReportProviderPrincipal(tokenAuthenticationContext, user, actingParty, capabilityTokenId, studyUIDs, source, clientId, hasReadAccess, hasWriteAccess, token);
    }
}
