package online.kheops.auth_server.filter;

import online.kheops.auth_server.annotation.TokenSecurity;
import online.kheops.auth_server.token.TokenClientAuthenticationType;
import online.kheops.auth_server.token.TokenPrincipal;
import org.glassfish.jersey.server.ContainerRequest;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

@TokenSecurity
@Provider
@Priority(Priorities.AUTHENTICATION)
public class TokenSecurityFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(TokenSecurityFilter.class.getName());

    @Context
    private ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final ContainerRequest containerRequest;
        if (requestContext instanceof ContainerRequest) {
            containerRequest = (ContainerRequest) requestContext;
        } else {
            throw new IllegalStateException("requestContext is not a ContainerRequest");
        }

        final Form form;
        final MultivaluedMap<String, String> requestHeaders;
        try {
            containerRequest.bufferEntity();
            form = containerRequest.readEntity(Form.class);
            requestHeaders = containerRequest.getRequestHeaders();
        } catch (ProcessingException e) {
            throw new IOException(e);
        }

        final TokenClientAuthenticationType authenticationType;
        try {
            authenticationType = TokenClientAuthenticationType.getAuthenticationType(requestHeaders, form);
        } catch (WebApplicationException e) {
            LOG.log(INFO, "Unable to find the authentication type", e);
            containerRequest.abortWith(e.getResponse());
            return;
        }

        final TokenPrincipal principal;
        try {
            final String relativePath = containerRequest.getBaseUri().relativize(containerRequest.getRequestUri()).toString();
            principal = authenticationType.authenticate(servletContext, "/api/" + relativePath, requestHeaders, form);
        } catch (WebApplicationException e) {
            LOG.log(INFO, "Unable to authenticate the client", e);
            containerRequest.abortWith(e.getResponse());
            return;
        }

        final boolean isSecured = requestContext.getSecurityContext().isSecure();

        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(String role) {
                return role.equals(principal.getClientKind().getRoleString());
            }

            @Override
            public boolean isSecure() {
                return isSecured;
            }

            @Override
            public String getAuthenticationScheme() {
                return authenticationType.getSchemeString();
            }
        });
    }
}
