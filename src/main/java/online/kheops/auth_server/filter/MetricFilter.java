package online.kheops.auth_server.filter;


import online.kheops.auth_server.annotation.MetricSecured;

import javax.annotation.Priority;
import javax.persistence.Tuple;
import javax.servlet.ServletContext;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE;

@MetricSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class MetricFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(MetricFilter.class.getName());

    @Context
    ServletContext servletContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        final BasicAuthentication basicAuthentication;
        try {
            basicAuthentication = getToken(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION));
            final String username = servletContext.getInitParameter("online.kheops.metric.authentication.username");
            final String password = servletContext.getInitParameter("online.kheops.metric.authentication.password");
            if (username.compareTo(basicAuthentication.userName) != 0 ||
                    password.compareTo(basicAuthentication.password) != 0) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .header(WWW_AUTHENTICATE,"Basic").build()) ;
            }
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "IllegalArgumentException " + getRequestString(requestContext), e);
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .header(WWW_AUTHENTICATE,"Basic").build());
        }

    }

    private static BasicAuthentication getToken(String authorizationHeader) {
        final BasicAuthentication basicAuthentication;
        if (authorizationHeader != null) {

            if (authorizationHeader.toUpperCase().startsWith("BASIC ")) {
                final String encodedAuthorization = authorizationHeader.substring(6);

                final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
                String[] split = decoded.split(":");
                if (split.length != 2) {
                    LOG.log(Level.WARNING, "Basic authentication doesn't have a username and password");
                    throw new IllegalArgumentException();
                }

                basicAuthentication = new BasicAuthentication(split[0], split[1]);
            } else {
                LOG.log(Level.WARNING, "Missing 'BASIC' authorization header");
                throw new IllegalArgumentException();
            }

        } else {
            LOG.log(Level.WARNING, "Missing authorization header");
            throw new IllegalArgumentException();
        }

        return basicAuthentication;
    }

    private static class BasicAuthentication {
        String userName;
        String password;

        public BasicAuthentication(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }

    private String getRequestString(ContainerRequestContext requestContext) {
        return requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri();
    }
}
