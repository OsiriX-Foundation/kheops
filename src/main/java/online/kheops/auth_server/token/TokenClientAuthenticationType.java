package online.kheops.auth_server.token;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_REQUEST;

public enum TokenClientAuthenticationType {

    CLIENT_SECRET_BASIC("client_secret_basic") {
        public TokenPrincipal authenticate(ServletContext context, String requestPath, MultivaluedMap<String, String> headers, Form form) {
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

            return TokenBasicAuthenticator.newAuthenticator(context).clientId(clientId).password(clientSecret).authenticate();
        }
    },

    PRIVATE_KEY_JWT("private_key_jwt") {
        public TokenPrincipal authenticate(ServletContext context, String requestPath, MultivaluedMap<String, String> headers, Form form) {
            MultivaluedMap<String, String> formParams = form.asMap();

            verifySingleHeader(formParams, CLIENT_ASSERTION);
            verifySingleHeader(formParams, CLIENT_ID);
            verifySingleHeader(formParams, CLIENT_ASSERTION_TYPE);

            if (!formParams.getFirst(CLIENT_ASSERTION_TYPE).equals(JWT_BEARER_URN)) {
                throw new TokenRequestException(INVALID_REQUEST, "Unknown client accesstoken type");
            }

            return PrivateKeyJWTAuthenticator.newAuthenticator(context)
                    .clientJWT(formParams.getFirst(CLIENT_ASSERTION))
                    .clientId(formParams.getFirst(CLIENT_ID))
                    .requestPath(requestPath)
                    .authenticate();
        }

        private void verifySingleHeader(MultivaluedMap<String, String> formParams, String header) {
            final List<String> headers = formParams.get(header);

            if (headers == null || headers.size() != 1) {
                throw new TokenRequestException(INVALID_REQUEST, "There isn't a single " + header + " header");
            }
        }
    },

    PUBLIC("public") {
        public TokenPrincipal authenticate(ServletContext context, String requestPath, MultivaluedMap<String, String> headers, Form form) {
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

    private String schemeString;

    TokenClientAuthenticationType(String schemeString) {
        this.schemeString = schemeString;
    }

    public String getSchemeString() {
        return schemeString;
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
                    return TokenClientAuthenticationType.PUBLIC;
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

        return TokenClientAuthenticationType.PUBLIC;
    }

    public abstract TokenPrincipal authenticate(ServletContext context, String requestPath, MultivaluedMap<String, String> headers, Form form);
}
