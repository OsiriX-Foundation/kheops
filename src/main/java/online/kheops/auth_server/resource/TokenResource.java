package online.kheops.auth_server.resource;


import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.AssertionVerifier;
import online.kheops.auth_server.entity.User;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Path("/")
public class TokenResource
{
    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        Long expiresIn;
    }

    static class ErrorResponse {
        String error;
        @XmlElement(name = "error_description")
        String errorDescription;
    }

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(@FormParam("grant_type") String grant_type, @FormParam("assertion") String assertion, @FormParam("scope") String scope)
        throws UnsupportedEncodingException {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.error = "invalid_grant";

        AssertionVerifier assertionVerifier = new AssertionVerifier(assertion, grant_type);

        if (assertionVerifier.isVerfified()) {
            EntityManagerFactory factory = Persistence.createEntityManagerFactory("online.kheops");
            EntityManager em = factory.createEntityManager();

            boolean newUser = false;
            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                User user = null;
                try {
                    TypedQuery<User> query = em.createQuery("select u from User u where u.username = :username", User.class);
                    user = query.setParameter("username", assertionVerifier.getUsername()).getSingleResult();
                } catch (Exception e) {
                    System.out.println("User not found");
                }

                if (user == null) {
                    newUser = true;
                    System.out.println("Creating a new User");
                    user = new User(assertionVerifier.getUsername(), assertionVerifier.getEmail());
                    em.persist(user);
                }
                tx.commit();
            } finally {
                em.close();
                factory.close();
            }

            // try to generate a new token

            final String HMAC256Secret = "P47dnfP28ptS/uzuuvEACmPYdMiOtFNLXiWTIwNNPgUjrvTgF/JCh3qZi47sIcpeZaUXw132mfmR4q5K/fwepA==";
            final Algorithm algorithmHMAC = Algorithm.HMAC256(HMAC256Secret);

            String token = JWT.create()
                    .withIssuer("auth.kheops.online")
                    .withSubject(assertionVerifier.getUsername())
                    .withAudience("dicom.kheops.online")
                    .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .withNotBefore(new Date())
                    .sign(algorithmHMAC);

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.accessToken = token;
            tokenResponse.tokenType = "Bearer";
            tokenResponse.expiresIn = 3600L;
            if (newUser) {
                return Response.status(201).build();
            } else {
                return Response.ok(tokenResponse).build();
            }
        } else {
            errorResponse.errorDescription = assertionVerifier.getErrorDescription();
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }
    }
}

