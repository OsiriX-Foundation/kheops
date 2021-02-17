package online.kheops.auth_server.filter;

import online.kheops.auth_server.accesstoken.AccessToken;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.accesstoken.AccessTokenVerifier;
import online.kheops.auth_server.accesstoken.AccessTokenVerificationException;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE;
import static javax.ws.rs.core.SecurityContext.BASIC_AUTH;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.Consts.USER_IN_ROLE;
import static online.kheops.auth_server.util.HttpHeaders.X_LINK_AUTHORIZATION;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(SecuredFilter.class.getName());

    public static final String BEARER_AUTH = "BEARER";
    public static final String LINK_AUTH = "LINK";

    @Context
    private ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        final Token token;
        try {
            token = getToken(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, String.format("IllegalArgumentException %s", getRequestString(requestContext)), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .header(WWW_AUTHENTICATE,"Basic").header(WWW_AUTHENTICATE,"Bearer").build());
            return;
        }

        final AccessToken accessToken;
        try {
            accessToken = AccessTokenVerifier.authenticateAccessToken(servletContext, token.getAccessToken());
        } catch (AccessTokenVerificationException e) {
            LOG.log(Level.WARNING, String.format("Received bad accesstoken%s", getRequestString(requestContext)), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        final User user;
        try {
            user = getUser(accessToken.getSubject());
        } catch (UserNotFoundException e) {
            LOG.log(Level.WARNING, String.format("User not found%s", requestContext.getUriInfo().getRequestUri()), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        final boolean isLink;
        if (requestContext.getHeaders().containsKey(X_LINK_AUTHORIZATION)) {
            isLink = Boolean.parseBoolean(requestContext.getHeaders().get(X_LINK_AUTHORIZATION).get(0));
        } else {
            isLink = false;
        }

        final boolean isSecured = requestContext.getSecurityContext().isSecure();
        final KheopsPrincipal principal = accessToken.newPrincipal(servletContext, user);
        principal.getKheopsLogBuilder().link(isLink);
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public KheopsPrincipal getUserPrincipal() { return principal; }

            @Override
            public boolean isUserInRole(String role) {
                if (role.equals(USER_IN_ROLE.CAPABILITY)) {
                    return accessToken.getTokenType() == AccessToken.TokenType.KEYCLOAK_TOKEN;
                } else if (role.equals(USER_IN_ROLE.VIEWER_TOKEN)) {
                    return accessToken.getTokenType() == AccessToken.TokenType.VIEWER_TOKEN;
                }
                return false;
            }

            @Override
            public boolean isSecure() {
                return isSecured;
            }

            @Override
            public String getAuthenticationScheme() {
                return isLink ? LINK_AUTH : token.getAuthenticationScheme();
            }
        });
    }

    public static Token getToken(String authorizationHeader) {
        final String token;
        final String authenticationScheme;

        if (authorizationHeader != null) {

            if (authorizationHeader.toUpperCase().startsWith("BASIC ")) {
                final String encodedAuthorization = authorizationHeader.substring(6);

                final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
                String[] split = decoded.split(":");
                if (split.length != 2) {
                    LOG.log(Level.WARNING, "Basic authentication doesn't have a username and password");
                    throw new IllegalArgumentException();
                }

                token = split[1];
                authenticationScheme = BASIC_AUTH;
            } else if (authorizationHeader.toUpperCase().startsWith("BEARER ")) {
                token = authorizationHeader.substring(7);
                authenticationScheme = BEARER_AUTH;
            } else {
                LOG.log(Level.WARNING, "Unknown authorization header");
                throw new IllegalArgumentException();
            }

            if (token.length() == 0) {
                LOG.log(Level.WARNING, "Empty authorization token");
                throw new IllegalArgumentException();
            }
        } else {
            LOG.log(Level.WARNING, "Missing authorization header");
            throw new IllegalArgumentException();
        }

        return new Token(token, authenticationScheme);
    }

    private String getRequestString(ContainerRequestContext requestContext) {
        return requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri();
    }

    public static class Token {
        private final String accessToken;
        private final String authenticationScheme;

        public Token(String accessToken, String authenticationScheme) {
            this.accessToken = accessToken;
            this.authenticationScheme = authenticationScheme;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getAuthenticationScheme() {
            return authenticationScheme;
        }
    }
}
