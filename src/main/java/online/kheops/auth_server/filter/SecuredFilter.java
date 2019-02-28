package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;
import online.kheops.auth_server.assertion.BadAssertionException;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.CapabilityPrincipal;
import online.kheops.auth_server.principal.KheopsPrincipalInterface;
import online.kheops.auth_server.principal.UserPrincipal;
import online.kheops.auth_server.principal.ViewerPrincipal;
import online.kheops.auth_server.user.UserNotFoundException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.user.Users.getOrCreateUser;
import static online.kheops.auth_server.util.Consts.USER_IN_ROLE;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(SecuredFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) {

        final String token;
        try {
            token = getToken(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "IllegalArgumentException " + getRequestString(requestContext), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(token);
        } catch (BadAssertionException e) {
            LOG.log(Level.WARNING, "Received bad assertion" + getRequestString(requestContext), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        final User user;
        try {
            user = getOrCreateUser(assertion.getSub());
        } catch (UserNotFoundException e) {
            LOG.log(Level.WARNING, "User not found" + requestContext.getUriInfo().getRequestUri(), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        final boolean capabilityAccess = assertion.hasCapabilityAccess();
        final boolean viewerTokenAccess = assertion.getViewer().isPresent();
        final boolean isSecured = requestContext.getSecurityContext().isSecure();
        final User finalUser = user;
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public KheopsPrincipalInterface getUserPrincipal() {
                if(assertion.getCapability().isPresent()) {
                    return new CapabilityPrincipal(assertion.getCapability().get(), finalUser);
                } else if(assertion.getViewer().isPresent()) {
                    return new ViewerPrincipal(assertion.getViewer().get());
                } else {
                    return new UserPrincipal(finalUser);
                }

                assertion.getViewer().ifPresent(this::new ViewerPrincipal());

            }

            @Override
            public boolean isUserInRole(String role) {
                if (role.compareTo(USER_IN_ROLE.CAPABILITY) == 0) {
                    return capabilityAccess;
                } else if (role.compareTo(USER_IN_ROLE.VIEWER_TOKEN) == 0) {
                    return viewerTokenAccess;
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
