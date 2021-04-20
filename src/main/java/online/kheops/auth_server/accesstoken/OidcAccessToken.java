package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.principal.UserPrincipal;
import online.kheops.auth_server.report_provider.OidcProviderException;
import online.kheops.auth_server.token.TokenAuthenticationContext;

import java.util.Optional;

public final class OidcAccessToken implements AccessToken {

    private final String subject;
    private final String actingParty;
    private final String token;

    private final String issuer;

    public static final class Builder implements AccessTokenBuilder {

        private final TokenAuthenticationContext tokenAuthenticationContext;

        public Builder(TokenAuthenticationContext tokenAuthenticationContext) {
            this.tokenAuthenticationContext = tokenAuthenticationContext;
        }

        public OidcAccessToken build(String assertionToken, boolean verifySignature) throws AccessTokenVerificationException {
            final DecodedJWT jwt;
            try {
                jwt = tokenAuthenticationContext.getOidcProvider().validateAccessToken(assertionToken, verifySignature);
            } catch (OidcProviderException e) {
                throw new AccessTokenVerificationException("Unable to validate the access token");
            }

            if (jwt.getSubject() == null) {
                throw new AccessTokenVerificationException("No subject present in the token");
            }

            final String oauthScope = servletContext.getInitParameter("online.kheops.oauth.scope");
            if (oauthScope != null && !oauthScope.isEmpty()) {
                final Claim scopeClaim = jwt.getClaim("scope");
                if (scopeClaim.isNull() || scopeClaim.asString() == null) {
                    throw new AccessTokenVerificationException("Missing scope claim in token");
                } else {
                    if (AccessTokenUtils.StringContainsScope(scopeClaim.asString(), oauthScope)) {
                        throw new AccessTokenVerificationException("Missing scope '" + oauthScope + "' in token");
                    }
                }
            }

            final String actingParty;
            Claim actClaim = jwt.getClaim("act");
            if (!actClaim.isNull()) {
                try {
                    actingParty = (String) actClaim.asMap().get("sub");
                } catch (ClassCastException | JWTDecodeException e) {
                    throw new AccessTokenVerificationException("Unable to read the acting party", e);
                }
                if (actingParty == null) {
                    throw new AccessTokenVerificationException("Has acting party, but without a subject");
                }
            } else {
                actingParty = null;
            }

            if (jwt.getIssuer() == null) {
                throw new AccessTokenVerificationException("No Issuer present in the token");
            }

            return new OidcAccessToken(jwt.getSubject(), actingParty, jwt.getIssuer(), assertionToken);
        }
    }

    public OidcAccessToken(String subject, String actingParty, String issuer, String token) {
        this.subject = subject;
        this.actingParty = actingParty;
        this.issuer = issuer;
        this.token = token;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public TokenType getTokenType() {
        return TokenType.KEYCLOAK_TOKEN;
    }

    @Override
    public Optional<String> getScope() {
        return Optional.of("user read write downloadbutton send");
    }

    @Override
    public Optional<String> getActingParty() {
        return Optional.ofNullable(actingParty);
    }

    @Override
    public KheopsPrincipal newPrincipal(TokenAuthenticationContext tokenAuthenticationContext, User user) {
        return new UserPrincipal(user, actingParty, token);
    }

    @Override
    public Optional<String> getIssuer() { return Optional.ofNullable(issuer); }
}
