package online.kheops.auth_server.filter;

import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;
import online.kheops.auth_server.assertion.BadAssertionException;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.resource.TokenResource;

import javax.annotation.Priority;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

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

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(token);
        } catch (BadAssertionException e) {
            LOG.log(Level.WARNING, "Received bad assertion", e);
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
            return;
        }

        User user = User.findByUsername(assertion.getUsername()).orElse(null);
        // if the user can't be found, try to build a new one;
        if (user == null) {
            // try to build a new user, building a new user might fail if there is a unique constraint violation
            // due to a race condition. Catch the violation and do nothing in that case.
            EntityManager em = EntityManagerListener.createEntityManager();
            EntityTransaction tx = em.getTransaction();

            try {
                tx.begin();
                LOG.info("User not found, creating a new User");
                User newUser = new User(assertion.getUsername(), assertion.getEmail());
                em.persist(newUser);
                tx.commit();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Caught exception while creating a new user", e);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }

            // At this point there should definitely be a user in the database for the calling user.
            user = User.findByUsername(assertion.getUsername()).orElseThrow(() -> new IllegalStateException("Unable to find user"));
        }

        final long userPK = user.getPk();
        final boolean capabilityAccess = assertion.hasCapabilityAccess();
        final boolean isSecured = requestContext.getSecurityContext().isSecure();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public KheopsPrincipal getUserPrincipal() {
                return new KheopsPrincipal(userPK);
            }

            @Override
            public boolean isUserInRole(String role) {
                if (role.equals("capability")) {
                    return capabilityAccess;
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
}
