package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.metadata.OidcMetadata;
import online.kheops.auth_server.report_provider.ReportProvider;
import online.kheops.auth_server.report_provider.ReportProviderNotFoundException;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalAuthMethodParameter.TOKEN_ENDPOINT_AUTH_METHOD;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalStringParameter.CLIENT_NAME;
import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_CLIENT;
import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_REQUEST;

public enum TokenClientAuthenticationType {

    CLIENT_SECRET_BASIC("client_secret_basic") {
        public TokenPrincipal authenticate(TokenAuthenticationContext context, String requestPath, MultivaluedMap<String, String> headers, Form form) {
            final String encodedAuthorization = headers.getFirst(AUTHORIZATION).substring(6);

            final String decoded;
            try {
                decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new TokenRequestException(INVALID_REQUEST, "Unable to decode Basic Authorization", e);
            }
            String[] split = decoded.split(":");
            if (split.length != 2) {
                throw new TokenRequestException(INVALID_REQUEST, "Basic authentication doesn't have a username and password");
            }

            final String clientId = split[0];
            final String clientSecret = split[1];

            return context.newTokenBasicAuthenticator().clientId(clientId).password(clientSecret).authenticate();
        }
    },

    PRIVATE_KEY_JWT("private_key_jwt") {
        public TokenPrincipal authenticate(TokenAuthenticationContext context, String requestPath, MultivaluedMap<String, String> headers, Form form) {
            MultivaluedMap<String, String> formParams = form.asMap();

            verifySingleHeader(formParams, CLIENT_ASSERTION);
            verifySingleHeader(formParams, CLIENT_ID);
            verifySingleHeader(formParams, CLIENT_ASSERTION_TYPE);

            if (!formParams.getFirst(CLIENT_ASSERTION_TYPE).equals(JWT_BEARER_URN)) {
                throw new TokenRequestException(INVALID_REQUEST, "Unknown client accesstoken type");
            }

            return context.newPrivateKeyJWTAuthenticator()
                    .clientJWT(formParams.getFirst(CLIENT_ASSERTION))
                    .clientId(formParams.getFirst(CLIENT_ID))
                    .requestPath(requestPath)
                    .authenticate();
        }
    },

    NONE("none") {
        public TokenPrincipal authenticate(TokenAuthenticationContext context, String requestPath, MultivaluedMap<String, String> headers, Form form) {
            MultivaluedMap<String, String> formParams = form.asMap();

            verifySingleHeader(formParams, CLIENT_ID);

            try {
                final ReportProvider reportProvider = context.getReportProviderCatalogue().getReportProvider(formParams.getFirst(CLIENT_ID));
                final OidcMetadata clientMetadata = reportProvider.getClientMetadata();

                if (!NONE.equals(clientMetadata.getValue(TOKEN_ENDPOINT_AUTH_METHOD).orElse(null))) {
                    throw new TokenRequestException(INVALID_CLIENT);
                }

                return new ReportProviderPrinciple() {
                    @Override
                    public String getName() {
                        return reportProvider.getClientMetadata().getValue(CLIENT_NAME).orElse("Unknown Report Provider");
                    }

                    @Override
                    public TokenClientKind getClientKind() {
                        return TokenClientKind.PUBLIC;
                    }

                    @Override
                    public ReportProvider getReportProvider() {
                        return reportProvider;
                    }
                };
            } catch (ReportProviderNotFoundException ignored) {
            }
            return PUBLIC_PRINCIPAL;
        }
    };

    private static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
    private static final String CLIENT_ASSERTION = "client_assertion";
    private static final String CLIENT_ID = "client_id";
    private static final String JWT_BEARER_URN = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    private static final TokenPrincipal PUBLIC_PRINCIPAL = new TokenPrincipal() {
        @Override
        public TokenClientKind getClientKind() {
            return TokenClientKind.PUBLIC;
        }

        @Override
        public String getName() {
            return "Public";
        }
    };

    private final String schemeString;

    TokenClientAuthenticationType(String schemeString) {
        this.schemeString = schemeString;
    }

    public String getSchemeString() {
        return schemeString;
    }

    public static TokenClientAuthenticationType fromSchemeString(String schemeString) {
        for (TokenClientAuthenticationType type: values()) {
            if (type.getSchemeString().equals(schemeString)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No such scheme string");
    }

    public static TokenClientAuthenticationType getAuthenticationType(MultivaluedMap<String, String> headers, Form form)
    {
        MultivaluedMap<String, String> formMap = form.asMap();

        if (headers.containsKey(AUTHORIZATION)) {
            if (formMap.containsKey(CLIENT_ASSERTION_TYPE) || formMap.containsKey(CLIENT_ASSERTION)) {
                throw new TokenRequestException(INVALID_REQUEST, "Client accesstoken and Authorization Header can not both be present");
            }

            List<String> authorizationHeaders = headers.get(AUTHORIZATION);

            if (authorizationHeaders == null || authorizationHeaders.size() != 1) {
                throw new TokenRequestException(INVALID_REQUEST, "Only one Authorization Header can be present");
            }
            try {
                if (authorizationHeaders.get(0).substring(0, 6).equalsIgnoreCase("Basic ")) {
                    return TokenClientAuthenticationType.CLIENT_SECRET_BASIC;
                }
                if (authorizationHeaders.get(0).substring(0, 7).equalsIgnoreCase("Bearer ")) {
                    return TokenClientAuthenticationType.NONE;
                }
            } catch (IndexOutOfBoundsException e) {
                throw new TokenRequestException(INVALID_REQUEST, "Unknown authorization type", e);
            }
            throw new TokenRequestException(INVALID_REQUEST, "Unknown authorization type");
        }

        if (formMap.containsKey(CLIENT_ASSERTION_TYPE) || formMap.containsKey(CLIENT_ASSERTION)) {
            List<String> clientAssertionTypes = formMap.get(CLIENT_ASSERTION_TYPE);
            List<String> clientAssertions = formMap.get(CLIENT_ASSERTION);

            if (clientAssertionTypes == null || clientAssertions == null ||
                    clientAssertionTypes.size() != 1 || clientAssertions.size() != 1) {
                throw new TokenRequestException(INVALID_REQUEST, "Only one client_assertion can be present");
            }
            if (!clientAssertionTypes.get(0).equals(JWT_BEARER_URN)) {
                throw new TokenRequestException(INVALID_REQUEST, "Unknown client_assertion_type");
            }
            if (clientAssertions.get(0).startsWith("eyJ")) {
                return TokenClientAuthenticationType.PRIVATE_KEY_JWT;
            } else {
                throw new TokenRequestException(INVALID_REQUEST, "Malformed client_assertion");
            }
        }

        return TokenClientAuthenticationType.NONE;
    }

    protected static void verifySingleHeader(MultivaluedMap<String, String> formParams, String header) {
        final List<String> headers = formParams.get(header);

        if (headers == null || headers.size() != 1) {
            throw new TokenRequestException(INVALID_REQUEST, "There isn't a single " + header + " header");
        }
    }

    public abstract TokenPrincipal authenticate(TokenAuthenticationContext context, String requestPath, MultivaluedMap<String, String> headers, Form form);
}
