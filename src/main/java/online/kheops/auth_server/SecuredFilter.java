package online.kheops.auth_server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
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
        long userPK;

        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();

        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            userPK = User.findPkByUsername(username, em);
            if (userPK == -1) {
                requestContext.abortWith(Response.status(403, "Unknown subject").build());
            }
            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        final boolean isSecured = requestContext.getSecurityContext().isSecure();
        final long finalUserPk = userPK;
        final boolean capabilityAccess;
        Claim capabilityClaim = jwt.getClaim("capability");
        if (capabilityClaim.asBoolean() != null) {
            capabilityAccess = capabilityClaim.asBoolean();
        } else {
            capabilityAccess = false;
        }

        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public KheopsPrincipal getUserPrincipal() {
                return new KheopsPrincipal(finalUserPk);
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
