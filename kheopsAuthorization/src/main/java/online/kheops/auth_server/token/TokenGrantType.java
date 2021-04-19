package online.kheops.auth_server.token;

import online.kheops.auth_server.accesstoken.AccessToken;
import online.kheops.auth_server.accesstoken.AccessTokenVerificationException;
import online.kheops.auth_server.accesstoken.AccessTokenVerifier;
import online.kheops.auth_server.report_provider.ClientIdNotFoundException;
import online.kheops.auth_server.report_provider.ReportProviderUriNotValidException;
import online.kheops.auth_server.report_provider.ReportProviders;
import online.kheops.auth_server.util.KheopsLogBuilder;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;

import java.util.ArrayList;
import java.util.List;

import static online.kheops.auth_server.accesstoken.AccessTokenUtils.StringContainsScope;
import static online.kheops.auth_server.accesstoken.AccessTokenUtils.ValidateScopeString;
import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.token.TokenRequestException.Error.*;
import static online.kheops.auth_server.util.Consts.INBOX;
import static online.kheops.auth_server.util.Tools.checkValidUID;

public enum TokenGrantType {
    REFRESH_TOKEN("refresh_token", KheopsLogBuilder.ActionType.REFRESH_TOKEN_GRANT) {
        public TokenGrantResult processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    AUTHORIZATION_CODE("authorization_code", KheopsLogBuilder.ActionType.AUTHORIZATION_CODE_GRANT) {
        public TokenGrantResult processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            verifySingleHeader(form, "code");
            verifySingleHeader(form, "client_id");
            verifySingleHeader(form, "redirect_uri");

            final String code = form.getFirst("code");
            final String clientId = form.getFirst("client_id");
            final String redirectUri = form.getFirst("redirect_uri");

            if (!securityContext.isUserInRole(TokenClientKind.REPORT_PROVIDER.getRoleString())) {
                throw new TokenRequestException(UNAUTHORIZED_CLIENT);
            }

            try {
                if (!ReportProviders.getRedirectUri(ReportProviders.getReportProvider(clientId)).equals(redirectUri)) {
                    throw new TokenRequestException(INVALID_GRANT, "redirect_uri does not match");
                }
            } catch (ReportProviderUriNotValidException | ClientIdNotFoundException e) {
                throw new TokenRequestException(INVALID_GRANT, e.getMessage(), e);
            }

            final DecodedAuthorizationCode authorizationCode;
            authorizationCode = AuthorizationCodeValidator.createAuthorizer(servletContext)
                    .withClientId(clientId)
                    .validate(code);

            ReportProviderAccessTokenGenerator generator = ReportProviderAccessTokenGenerator.createGenerator(servletContext)
                    .withSubject(authorizationCode.getSubject())
                    .withClientId(clientId)
                    .withStudyInstanceUIDs(authorizationCode.getStudyInstanceUIDs())
                    .withScope("read write");

            authorizationCode.getActingParty().ifPresent(generator::withActingParty);
            authorizationCode.getCapabilityTokenId().ifPresent(generator::withCapabilityTokenId);

            final String token = generator.generate(REPORT_PROVIDER_TOKEN_LIFETIME);

            return new TokenGrantResult(TokenResponseEntity.createEntity(token, REPORT_PROVIDER_TOKEN_LIFETIME), authorizationCode.getSubject());
        }
    },
    PASSWORD("password", KheopsLogBuilder.ActionType.PASSWORD_GRANT) {
        public TokenGrantResult processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    CLIENT_CREDENTIALS("client_credentials", KheopsLogBuilder.ActionType.CLIENT_CREDENTIALS_GRANT) {
        public TokenGrantResult processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    JWT_ASSERTION("urn:ietf:params:oauth:grant-type:jwt-bearer", KheopsLogBuilder.ActionType.JWT_ASSERTION_GRANT) {
        public TokenGrantResult processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    SAML_ASSERTION("urn:ietf:params:oauth:grant-type:saml2-bearer", KheopsLogBuilder.ActionType.SAML_ASSERTION_GRANT) {
        public TokenGrantResult processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    TOKEN_EXCHANGE("urn:ietf:params:oauth:grant-type:token-exchange", KheopsLogBuilder.ActionType.TOKEN_EXCHANGE_GRANT) {
        public TokenGrantResult processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            verifySingleHeader(form, "scope");
            verifySingleHeader(form, "subject_token");
            verifySingleHeader(form, "subject_token_type");
            verifySingleHeader(form, "studyUID");

            final String scopeString = form.getFirst("scope");
            final String subjectToken = form.getFirst("subject_token");
            final String subjectTokenType = form.getFirst("subject_token_type");
            final String studyInstanceUID = form.getFirst("studyUID");
            if (!checkValidUID(studyInstanceUID)) {
                throw new TokenRequestException(INVALID_REQUEST, "Bad study instance UID");
            }

            if (!subjectTokenType.equals("urn:ietf:params:oauth:token-type:access_token")) {
                throw new TokenRequestException(INVALID_REQUEST, "Unknown subject token type");
            }
            if (!ValidateScopeString(scopeString)) {
                throw new TokenRequestException(INVALID_SCOPE);
            }

            final String subject;
            final TokenProvenance tokenProvenance;
            try {
                AccessToken accessToken = AccessTokenVerifier.authenticateAccessToken(servletContext, subjectToken);
                tokenProvenance = accessToken;
                subject = accessToken.getSubject();
            } catch (AccessTokenVerificationException e) {
                throw new TokenRequestException(INVALID_REQUEST, "Bad subject_token", e);
            }

            if (StringContainsScope(scopeString, "pep")) {
                verifySingleHeader(form, "seriesUID");
                final String seriesInstanceUID = form.getFirst("seriesUID");
                if (!checkValidUID(seriesInstanceUID)) {
                    throw new TokenRequestException(INVALID_REQUEST, "Bad series instance UID");
                }

                if (!securityContext.isUserInRole(TokenClientKind.INTERNAL.getRoleString())) {
                    throw new TokenRequestException(UNAUTHORIZED_CLIENT);
                }

                final String pepToken = PepAccessTokenGenerator.createGenerator(servletContext)
                        .withToken(subjectToken)
                        .withStudyInstanceUID(studyInstanceUID)
                        .withSeriesInstanceUID(seriesInstanceUID)
                        .generate(PEP_TOKEN_LIFETIME);

                return new TokenGrantResult(TokenResponseEntity.createEntity(pepToken, PEP_TOKEN_LIFETIME), subject, tokenProvenance, "pep", studyInstanceUID, seriesInstanceUID);
            } else if (StringContainsScope(scopeString,"viewer")) {
                verifyNotDuplicateHeader(form, "source_type");
                final String sourceType = form.getFirst("source_type");

                final String sourceId;
                if (sourceType != null && !(sourceType.equals(INBOX) || sourceType.equals(ALBUM))) {
                    throw new TokenRequestException(INVALID_REQUEST, "sourceType must be either album or inbox");
                }
                if (sourceType != null && sourceType.equals(ALBUM)) {
                    verifySingleHeader(form, "source_id");
                    sourceId = form.getFirst("source_id");
                } else {
                    if (form.get("source_id") != null) {
                        throw new TokenRequestException(INVALID_REQUEST, "source_id should not be specified for non-album sources");
                    }
                    sourceId = null;
                }

                final List<String> scopes = new ArrayList<>();
                if (StringContainsScope(scopeString,"read")) {
                    scopes.add("read");
                }
                if (StringContainsScope(scopeString,"write")) {
                    scopes.add("write");
                }

                final String viewerToken = ViewerAccessTokenGenerator.createGenerator(servletContext)
                        .withToken(subjectToken)
                        .withStudyInstanceUID(studyInstanceUID)
                        .withSourceType(sourceType)
                        .withSourceId(sourceId)
                        .withScopes(scopes)
                        .generate(VIEWER_TOKEN_LIFETIME);

                return new TokenGrantResult(TokenResponseEntity.createEntity(viewerToken, VIEWER_TOKEN_LIFETIME), subject, tokenProvenance, "viewer", studyInstanceUID, null);
            } else {
                throw new TokenRequestException(INVALID_SCOPE);
            }
        }
    };

    private static final long REPORT_PROVIDER_TOKEN_LIFETIME = 60L * 60L; // 1 hour
    private static final long PEP_TOKEN_LIFETIME = 60L * 60L; // 1 hours
    private static final long VIEWER_TOKEN_LIFETIME = 60L * 60L * 5L; // 5 hours

    private final String grantType;
    private final KheopsLogBuilder.ActionType logActionType;

    TokenGrantType(final String grantType, KheopsLogBuilder.ActionType logActionType) {
        this.grantType = grantType;
        this.logActionType = logActionType;
    }

    @Override
    public String toString() {
        return grantType;
    }

    public static TokenGrantType from(String grantTypeString) {
        for (TokenGrantType grantType: TokenGrantType.values()) {
            if (grantType.toString().equals(grantTypeString)) {
                return grantType;
            }
        }

        throw new IllegalArgumentException("Unknown grant type");
    }

    public abstract TokenGrantResult processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form);

    private static void verifySingleHeader(final MultivaluedMap<String, String> form, final String param) {
        final List<String> params = form.get(param);
        if (params == null || params.size() != 1) {
            throw new TokenRequestException(INVALID_REQUEST, "Must have a single " + param);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void verifyNotDuplicateHeader(final MultivaluedMap<String, String> form, final String param) {
        final List<String> params = form.get(param);
        if (params != null && params.size() > 1) {
            throw new TokenRequestException(INVALID_REQUEST, "Must not have multiple " + param);
        }
    }

    public KheopsLogBuilder.ActionType getLogActionType() {
        return logActionType;
    }
}
