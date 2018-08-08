package online.kheops.auth_server.filter;

import online.kheops.auth_server.Capabilities;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.assertion.assertion.AccessJWTAssertion;
import online.kheops.auth_server.assertion.assertion.CapabilityAssertion;
import online.kheops.auth_server.assertion.assertion.GoogleJWTAssertion;
import online.kheops.auth_server.assertion.assertion.SuperuserJWTAssertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;
import online.kheops.auth_server.entity.User;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {
    @Context
    ServletContext context;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Bearer authorization header must be provided");
        }

        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();

        long userPK = -1;
        boolean capabilityAccess = false;
        boolean validToken = false;

        if (Capabilities.isValidFormat(token)) {
            final CapabilityAssertion capabilityAssertion = new CapabilityAssertion();

            try {
                capabilityAssertion.setCapabilityToken(token);
            } catch (NotAuthorizedException e) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            } catch (ForbiddenException e) {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
                return;
            }

            userPK = User.findByUsername(capabilityAssertion.getUsername()).getPk();
            capabilityAccess = capabilityAssertion.getCapabilityAccess();
            validToken = true;

        } else {

            final GoogleJWTAssertion googleJWTAssertion = new GoogleJWTAssertion();
            try {
                googleJWTAssertion.setAssertionToken(token);
                userPK = User.findByUsername(googleJWTAssertion.getUsername()).getPk();
                capabilityAccess = true;
                validToken = true;

            } catch (BadAssertionException e) { /*empty*/ }

            if ( ! validToken) {
                final AccessJWTAssertion accessJWTAssertion = new AccessJWTAssertion();
                try {
                    accessJWTAssertion.setAssertionToken(token, context);
                    userPK = User.findByUsername(accessJWTAssertion.getUsername()).getPk();
                    capabilityAccess = accessJWTAssertion.getCapabilityAccess();
                    validToken = true;
                } catch (BadAssertionException e) {/*empty*/}
            }

            if ( ! validToken) {
                final SuperuserJWTAssertion superuserJWTAssertion = new SuperuserJWTAssertion();
                try {
                    superuserJWTAssertion.setAssertionToken(token);
                    userPK = User.findByUsername(superuserJWTAssertion.getUsername()).getPk();
                    capabilityAccess = superuserJWTAssertion.getCapabilityAccess();
                    validToken = true;
                } catch (BadAssertionException e) {/*empty*/}
                  catch (Exception e) {/*empty*/}

            }
        }

        if (validToken) {
            Response.Status responseStatus = Response.Status.OK;
            final boolean isSecured = requestContext.getSecurityContext().isSecure();
            final long finalUserPk = userPK;
            final boolean finalCapabilityAccess = capabilityAccess;

            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public KheopsPrincipal getUserPrincipal() {
                    return new KheopsPrincipal(finalUserPk);
                }

                @Override
                public boolean isUserInRole(String role) {
                    if (role.equals("capability")) {
                        return finalCapabilityAccess;
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
        } else {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
