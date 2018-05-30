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
import online.kheops.auth_server.*;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;
import online.kheops.auth_server.assertion.exceptions.DownloadKeyException;
import online.kheops.auth_server.assertion.exceptions.UnknownGrantTypeException;
import online.kheops.auth_server.entity.User;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public class TokenResource
{

    private static final Logger LOG = Logger.getLogger(TokenResource.class.getName());

    @Context
    ServletContext context;

    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        Long expiresIn;
        @XmlElement(name = "username")
        String username;
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
    public Response token(@FormParam("grant_type") String grantType, @FormParam("assertion") String assertionToken, @FormParam("scope") String scope) {

        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.error = "invalid_grant";

        if (grantType == null) {
            errorResponse.errorDescription = "Missing grant_type";
            LOG.warning("Request for a token is missing a grant_type");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }
        if (assertionToken == null) {
            errorResponse.errorDescription = "Missing assertion";
            LOG.warning("Request for a token is missing an assertion");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        }

        final Assertion assertion;
        try {
            assertion = AssertionVerifier.createAssertion(assertionToken, grantType);
        } catch (UnknownGrantTypeException | BadAssertionException e) {
            errorResponse.errorDescription = e.getMessage();
            LOG.log(Level.WARNING, "Error validating a token", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        } catch (DownloadKeyException e) {
            LOG.log(Level.SEVERE, "Error downloading public keys", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        final EntityManager em = EntityManagerListener.createEntityManager();
        final EntityTransaction tx = em.getTransaction();
        Response.Status responseStatus = Response.Status.OK;
        try {
            tx.begin();

            long userPk = User.findPkByUsername(assertion.getUsername(), em);
            if (userPk == -1) {
                LOG.info("User not found, creating a new User");
                responseStatus = Response.Status.CREATED;
                em.persist(new User(assertion.getUsername(), assertion.getEmail()));
            }
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }

        // Generate a new token
        final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecret");
        final Algorithm algorithmHMAC;
        try {
            algorithmHMAC = Algorithm.HMAC256(authSecret);
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "online.kheops.auth.hmacsecret is not a valid HMAC secret", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        String token = JWT.create()
                .withIssuer("auth.kheops.online")
                .withSubject(assertion.getUsername())
                .withAudience("dicom.kheops.online")
                .withClaim("capability", !assertion.isCapabilityAssertion()) // don't give capability access for capability assertions
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .withNotBefore(new Date())
                .sign(algorithmHMAC);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.accessToken = token;
        tokenResponse.tokenType = "Bearer";
        tokenResponse.expiresIn = 3600L;
        if (assertion.isCapabilityAssertion()) {
            tokenResponse.username = assertion.getUsername();
        }
        return Response.status(responseStatus).entity(tokenResponse).build();
    }
}

