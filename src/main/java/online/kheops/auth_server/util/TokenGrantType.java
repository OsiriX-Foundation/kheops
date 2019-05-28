package online.kheops.auth_server.util;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.util.List;

import static online.kheops.auth_server.util.Consts.ALBUM;
import static online.kheops.auth_server.util.TokenRequestException.Error.*;
import static online.kheops.auth_server.util.Tools.checkValidUID;

public enum TokenGrantType {
    REFRESH_TOKEN("refresh_token") {
        public Response processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    AUTHORIZATION_CODE("authorization_code") {
        public Response processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            verifySingleHeader(form, "code");
            verifySingleHeader(form, "client_id");

            final String code = form.getFirst("code");
            final String clientId = form.getFirst("client_id");

            if (!securityContext.isUserInRole(TokenClientKind.REPORT_PROVIDER.getRoleString())) {
                throw new TokenRequestException(UNAUTHORIZED_CLIENT);
            }

            final DecodedAuthorizationCode authorizationCode;
            try {
                authorizationCode = AuthorizationCodeValidator.createAuthorizer(servletContext)
                        .withClientId(clientId)
                        .validate(code);
            } catch (TokenAuthenticationException e) {
                throw new TokenRequestException(UNAUTHORIZED_CLIENT, e);
            }

            final String token;
            try {
                token = ReportProviderTokenGenerator.createGenerator(servletContext)
                        .withSubject(authorizationCode.getSubject())
                        .withClientId(clientId)
                        .withStudyInstanceUIDs(authorizationCode.getStudyInstanceUIDs())
                        .generate(REPORT_PROVIDER_TOKEN_LIFETIME);
            } catch (TokenAuthenticationException e) {
                throw new TokenRequestException(UNAUTHORIZED_CLIENT, e.getMessage(), e);
            }

            return Response.ok(TokenResponseEntity.createEntity(token, REPORT_PROVIDER_TOKEN_LIFETIME)).build();
        }
    },
    PASSWORD("password") {
        public Response processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    CLIENT_CREDENTIALS("client_credentials") {
        public Response processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    JWT_ASSERTION("urn:ietf:params:oauth:grant-type:jwt-bearer") {
        public Response processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    SAML_ASSERTION("urn:ietf:params:oauth:grant-type:saml2-bearer") {
        public Response processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            throw new TokenRequestException(UNSUPPORTED_GRANT_TYPE);
        }
    },
    TOKEN_EXCHANGE("urn:ietf:params:oauth:grant-type:token-exchange") {
        public Response processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form) {
            verifySingleHeader(form, "scope");
            verifySingleHeader(form, "subject_token");
            verifySingleHeader(form, "subject_token_type");
            verifySingleHeader(form, "studyUID");

            final String scope = form.getFirst("scope");
            final String subjectToken = form.getFirst("subject_token");
            final String subjectTokenType = form.getFirst("subject_token_type");
            final String studyInstanceUID = form.getFirst("studyUID");
            if (!checkValidUID(studyInstanceUID)) {
                throw new TokenRequestException(INVALID_REQUEST, "Bad study instance UID");
            }

            if (!subjectTokenType.equals("urn:ietf:params:oauth:token-type:access_token")) {
                throw new TokenRequestException(INVALID_REQUEST, "Unknown subject token type");
            }

            if (scope.equals("pep")) {
                verifySingleHeader(form, "seriesUID");
                final String seriesInstanceUID = form.getFirst("seriesUID");
                if (!checkValidUID(seriesInstanceUID)) {
                    throw new TokenRequestException(INVALID_REQUEST, "Bad series instance UID");
                }

                if (!securityContext.isUserInRole(TokenClientKind.INTERNAL.getRoleString())) {
                    throw new TokenRequestException(UNAUTHORIZED_CLIENT);
                }

                String pepToken = PepTokenGenerator.createGenerator(servletContext)
                        .withToken(subjectToken)
                        .withStudyInstanceUID(studyInstanceUID)
                        .withSeriesInstanceUID(seriesInstanceUID)
                        .generate(PEP_TOKEN_LIFETIME);
                return Response.ok(TokenResponseEntity.createEntity(pepToken, PEP_TOKEN_LIFETIME)).build();
            } else if (scope.equals("viewer")) {
                verifySingleHeader(form, "source_type");
                final String sourceType = form.getFirst("source_type");

                final String sourceId;
                if (sourceType.equals(ALBUM)) {
                    verifySingleHeader(form, "source_id");
                    sourceId = form.getFirst("source_id");
                } else {
                    if (form.get("source_id") == null) {
                        throw new TokenRequestException(INVALID_REQUEST, "source_id should not be specified for non-album sources");
                    }
                    sourceId = null;
                }

                String viewerToken = ViewerTokenGenerator.createGenerator()
                        .withToken(subjectToken)
                        .withStudyInstanceUID(studyInstanceUID)
                        .withSourceType(sourceType)
                        .withSourceId(sourceId)
                        .generate(VIEWER_TOKEN_LIFETIME);
                return Response.ok(TokenResponseEntity.createEntity(viewerToken, VIEWER_TOKEN_LIFETIME)).build();
            } else {
                throw new TokenRequestException(INVALID_SCOPE);
            }
        }
    };

    final private static long REPORT_PROVIDER_TOKEN_LIFETIME = 60 * 60 * 5; // 5 hours
    final private static long PEP_TOKEN_LIFETIME = 60 * 60; // 1 hours
    final private static long VIEWER_TOKEN_LIFETIME = 60 * 60 * 5; // 5 hours

    private final String grantType;

    TokenGrantType(final String grantType) {
        this.grantType = grantType;
    }

    public String toString() {
        return grantType;
    }

    public static TokenGrantType fromString(String grantTypeString) {
        for (TokenGrantType grantType: TokenGrantType.values()) {
            if (grantType.toString().equals(grantTypeString)) {
                return grantType;
            }
        }

        throw new IllegalArgumentException("Unknown grant type");
    }

    public abstract Response processGrant(SecurityContext securityContext, ServletContext servletContext, MultivaluedMap<String, String> form);

    private static void verifySingleHeader(final MultivaluedMap<String, String> form, final String param) throws TokenRequestException {
        final List<String> params = form.get(param);
        if (params == null || form.get(param).size() != 1) {
            throw new TokenRequestException(INVALID_REQUEST, "Must have a single " + param);
        }
    }
}
