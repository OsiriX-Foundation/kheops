package online.kheops.auth_server.resource;


import javax.persistence.*;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.AssertionVerifier;
import online.kheops.auth_server.PersistenceUtils;
import online.kheops.auth_server.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Path("/")
public class TokenResource
{

    private static final Logger LOG = LoggerFactory.getLogger(TokenResource.class);

    @Context
    ServletContext context;

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


        if (grant_type == null) {
            errorResponse.errorDescription = "Missing grant_type";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }
        if (assertion == null) {
            errorResponse.errorDescription = "Missing assertion";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        final String superuserSecret = context.getInitParameter("online.kheops.superuser.hmacsecret");
        AssertionVerifier assertionVerifier = new AssertionVerifier(assertion, grant_type, superuserSecret);

        if (assertionVerifier.isVerfified()) {
            EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
            EntityManager em = factory.createEntityManager();

            boolean newUser = false;
            try {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                long userPk = User.findPkByUsername(assertionVerifier.getUsername(), em);
                if (userPk == -1) {
                    LOG.info("User not found, creating a new User");
                    newUser = true;
                    em.persist(new User(assertionVerifier.getUsername(), assertionVerifier.getEmail()));
                }
                tx.commit();
            } catch (Throwable t) {
                LOG.error("Couldn't find the user", t);
            } finally {
                em.close();
                factory.close();
            }

            // Generate a new token

            final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecret");
            final Algorithm algorithmHMAC = Algorithm.HMAC256(authSecret);

            String token = JWT.create()
                    .withIssuer("auth.kheops.online")
                    .withSubject(assertionVerifier.getUsername())
                    .withAudience("dicom.kheops.online")
                    .withClaim("capability", !assertionVerifier.isCapabilityAssertion()) // don't give capability access for capability assertions
                    .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .withNotBefore(new Date())
                    .sign(algorithmHMAC);

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.accessToken = token;
            tokenResponse.tokenType = "Bearer";
            tokenResponse.expiresIn = 3600L;
            if (newUser) {
                return Response.status(Response.Status.CREATED).entity(tokenResponse).build();
            } else {
                return Response.ok(tokenResponse).build();
            }
        } else {
            errorResponse.errorDescription = assertionVerifier.getErrorDescription();
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }
    }
}

