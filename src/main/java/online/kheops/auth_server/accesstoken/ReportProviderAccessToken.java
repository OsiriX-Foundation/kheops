package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.*;

import javax.servlet.ServletContext;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ReportProviderAccessToken implements AccessToken {
    private final String sub;
    private final List<String> studyUIDs;
    private final String clientId;
    private final boolean hasReadAccess;
    private final boolean hasWriteAccess;

    static class Builder implements AccessTokenBuilder{
        private static final String HOST_ROOT_PARAMETER = "online.kheops.root.uri";

        private final ServletContext servletContext;

        Builder(ServletContext servletContext) {
            this.servletContext = Objects.requireNonNull(servletContext);
        }

        public ReportProviderAccessToken build(String assertionToken) throws AccessTokenVerificationException {
            Objects.requireNonNull(assertionToken);

            final Algorithm algorithm;
            try {
                algorithm = Algorithm.HMAC256(authorizationSecret());
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("authorizationSecret is not a valid HMAC256 secret", e);
            }
            final DecodedJWT jwt;
            try {
                jwt = JWT.require(algorithm)
                        .withIssuer(getIssuerHost())
                        .withAudience(getIssuerHost())
                        .withClaim("type", "report_generator")
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

            boolean hasReadAccess = false;
            boolean hasWriteAccess = false;
            final String[] scopeWords = scopeClaim.asString().split("\\s+", 40);
            for (String scopeWord : scopeWords) {
                if (scopeWord.equals("read")) {
                    hasReadAccess = true;
                }
                if (scopeWord.equals("write")) {
                    hasWriteAccess = true;
                }
            }

            final Claim studyUIDsClaim = jwt.getClaim("study_uids");
            try {
                if (studyUIDsClaim.isNull() || studyUIDsClaim.asList(String.class) == null) {
                    throw new AccessTokenVerificationException("Missing study_uids claim in token");
                }
                return new ReportProviderAccessToken(jwt.getSubject(), studyUIDsClaim.asList(String.class), azpClaim.asString(), hasReadAccess, hasWriteAccess);
            } catch (JWTDecodeException e) {
                throw new AccessTokenVerificationException("unable to decode study_uids");
            }
        }

        private String authorizationSecret() {
            return servletContext.getInitParameter("online.kheops.auth.hmacsecret");
        }

        private String getIssuerHost() {
            return servletContext.getInitParameter(HOST_ROOT_PARAMETER);
        }

    }

    private ReportProviderAccessToken(String sub, List<String> studyUIDs, String ClientId, boolean hasReadAccess, boolean hasWriteAccess) {
        this.sub = Objects.requireNonNull(sub);
        this.studyUIDs = Objects.requireNonNull(studyUIDs);
        this.clientId = Objects.requireNonNull(ClientId);
        this.hasReadAccess = hasReadAccess;
        this.hasWriteAccess = hasWriteAccess;
    }

    @Override
    public String getSub() {
        return sub;
    }

    @Override
    public TokenType getTokenType() { return TokenType.REPORT_PROVIDER_TOKEN; }

    @Override
    public Optional<String> getScope() {
        return Optional.of("read write");
    }

    @Override
    public KheopsPrincipalInterface newPrincipal(ServletContext servletContext, User user) {
        return new ReportProviderPrincipal(user, studyUIDs, clientId, hasReadAccess, hasWriteAccess);
    }
}
