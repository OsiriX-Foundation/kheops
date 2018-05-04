package online.kheops.auth_server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.entity.User;

import javax.annotation.Priority;
import javax.persistence.*;
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
import java.io.IOException;
import java.security.Principal;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredFilter implements ContainerRequestFilter {
    @Context
    ServletContext context;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Bearer authorization header must be provided");
        }

        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();

        final DecodedJWT jwt;
        try {
            final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecret");
            final Algorithm kheopsAlgorithmHMAC = Algorithm.HMAC256(authSecret);
            JWTVerifier verifier = JWT.require(kheopsAlgorithmHMAC)
                    .withIssuer("auth.kheops.online")
                    .build();
            jwt = verifier.verify(token);
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }
        final String username = jwt.getSubject();

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
        EntityManager em = factory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.username = :username", User.class);
            userQuery.setParameter("username", username).getSingleResult();
        } catch (Exception exception) {
            requestContext.abortWith(Response.status(403, "Unknown subject").build());
        } finally {
            em.close();
            factory.close();
        }

        boolean isSecured = requestContext.getSecurityContext().isSecure();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override public Principal getUserPrincipal() { return () -> username; }
            @Override public boolean isUserInRole(String role) { return true; }
            @Override public boolean isSecure() { return isSecured; }
            @Override public String getAuthenticationScheme() { return "BEARER"; }
        });
    }
}
