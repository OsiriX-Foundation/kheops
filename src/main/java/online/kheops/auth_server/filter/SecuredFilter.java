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
import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.USER_IN_ROLE;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(SecuredFilter.class.getName());

    @Context
    private ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        final String token;
        try {
            token = getToken(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "IllegalArgumentException " + getRequestString(requestContext), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .header(WWW_AUTHENTICATE,"Basic").header(WWW_AUTHENTICATE,"Bearer").build());
            return;
        }

        boolean linkAuthorization;
        try {
            linkAuthorization = Boolean.valueOf(requestContext.getHeaderString("X-Link-Authorization"));
        } catch (Exception e) {
            linkAuthorization = false;
        }

        final AccessToken accessToken;
        try {
            accessToken = AccessTokenVerifier.authenticateAccessToken(servletContext, token, linkAuthorization);
        } catch (AccessTokenVerificationException e) {
            LOG.log(Level.WARNING, "Received bad accesstoken" + getRequestString(requestContext), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }


        final User user;
        try {
            user = getOrCreateUser(accessToken.getSubject());
        } catch (UserNotFoundException e) {
            LOG.log(Level.WARNING, "User not found" + requestContext.getUriInfo().getRequestUri(), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        final boolean isSecured = requestContext.getSecurityContext().isSecure();
        final User finalUser = user;
        final boolean isLinkAuthorization = linkAuthorization;
        final KheopsPrincipal principal = accessToken.newPrincipal(servletContext, finalUser);
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
                return "BEARER";
            }
        });
    }

    private static String getToken(String authorizationHeader) {
        final String token;
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
            } else if (authorizationHeader.toUpperCase().startsWith("BEARER ")) {
                token = authorizationHeader.substring(7);
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

        return token;
    }

    private String getRequestString(ContainerRequestContext requestContext) {
        return requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri();
    }
}
