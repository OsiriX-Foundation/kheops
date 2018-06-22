package online.kheops.auth_server.resource;


import javax.persistence.*;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.*;
import online.kheops.auth_server.annotation.FormURLEncodedContentType;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.AssertionVerifier;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;
import online.kheops.auth_server.assertion.exceptions.DownloadKeyException;
import online.kheops.auth_server.assertion.exceptions.UnknownGrantTypeException;
import online.kheops.auth_server.entity.User;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public class TokenResource
{
    private static class UIDPair {
        private String studyInstanceUID;
        private String seriesInstanceUID;

        @SuppressWarnings("WeakerAccess")
        public UIDPair(String studyInstanceUID, String seriesInstanceUID) {
            this.studyInstanceUID = studyInstanceUID;
            this.seriesInstanceUID = seriesInstanceUID;
        }

        String getStudyInstanceUID() {
            return studyInstanceUID;
        }

        String getSeriesInstanceUID() {
            return seriesInstanceUID;
        }
    }

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
        @XmlElement(name = "user")
        String user;
    }

    static class ErrorResponse {
        String error;
        @XmlElement(name = "error_description")
        String errorDescription;
    }

    @POST
    @FormURLEncodedContentType
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response token(@FormParam("grant_type") String grantType, @FormParam("assertion") String assertionToken, @FormParam("scope") String scope) {

        UIDPair uidPair = getUIDPairFromScope(scope);
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
            LOG.log(Level.SEVERE, "Error downloading the public key", e);
            errorResponse.error = "server_error";
            errorResponse.errorDescription = "error";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }

        Response.Status responseStatus = Response.Status.OK;

        EntityManager em;
        EntityTransaction tx;

        // try to find the user in the database;
        User callingUser = User.findByUsername(assertion.getUsername());

        // if the user can't be found, try to build a new one;
        if (callingUser == null) {
            // try to build a new user, building a new user might fail if there is a unique constraint violation
            // due to a race condition. Catch the violation and do nothing in that case.
            em = EntityManagerListener.createEntityManager();
            tx = em.getTransaction();

            try {
                tx.begin();
                LOG.info("User not found, creating a new User");
                User user = new User(assertion.getUsername(), assertion.getEmail());
                em.persist(user);
                tx.commit();
                responseStatus = Response.Status.CREATED;
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Caught exception while creating a new user", e);
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }

            // At this point there should definitely be a user in the database for the calling user.
            callingUser = User.findByUsername(assertion.getUsername());
        }

        em = EntityManagerListener.createEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();

            if (uidPair != null && callingUser != null) {
                if (!callingUser.hasAccess(uidPair.getStudyInstanceUID(), uidPair.getSeriesInstanceUID(), em)) {
                    LOG.info("The user does not have access to the given StudyInstanceUID and SeriesInstanceUID pair");
                    errorResponse.errorDescription = "The user does not have access to the given StudyInstanceUID and SeriesInstanceUID pair";
                    return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
                }
            }

            tx.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while accessing the database", e);
            errorResponse.error = "server_error";
            errorResponse.errorDescription = "error";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
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

        JWTCreator.Builder jwtBuilder = JWT.create()
                .withIssuer("auth.kheops.online")
                .withSubject(assertion.getUsername())
                .withAudience("dicom.kheops.online")
                .withClaim("capability", !assertion.isCapabilityAssertion()) // don't give capability access for capability assertions
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .withNotBefore(new Date());

        if (uidPair != null) {
            jwtBuilder = jwtBuilder.withClaim("study_uid", uidPair.getStudyInstanceUID())
                .withClaim("series_uid", uidPair.getSeriesInstanceUID());
        }

        String token = jwtBuilder.sign(algorithmHMAC);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.accessToken = token;
        tokenResponse.tokenType = "Bearer";
        tokenResponse.expiresIn = 3600L;
        if (assertion.isCapabilityAssertion()) {
            tokenResponse.user = assertion.getUsername();
        }
        LOG.info("Returning token for user: " + assertion.getUsername());
        return Response.status(responseStatus).entity(tokenResponse).build();
    }

    private UIDPair getUIDPairFromScope(String scope) {
        String seriesUID = null;
        String studyUID = null;
        final ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.error = "invalid_grant";

        if (scope == null) {
            return null;
        }

       String[] scopes = scope.split(" ");
       for (String item: scopes) {
           if (item.startsWith("StudyInstanceUID=")) {
               studyUID = item.substring(17);
               try {
                   new Oid(studyUID);
               } catch (GSSException exception) {
                   LOG.info("scope StudyInstanceUID is not a valid UID");
                   errorResponse.errorDescription = "scope StudyInstanceUID is not a valid UID";
                   throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build());
               }
           } else if (item.startsWith("SeriesInstanceUID=")) {
               seriesUID = item.substring(18);
               try {
                   new Oid(seriesUID);
               } catch (GSSException exception) {
                   LOG.info("scope SeriesInstanceUID is not a valid UID");
                   errorResponse.errorDescription = "scope SeriesInstanceUID is not a valid UID";
                   throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build());
               }
           } else {
               LOG.info("Unknown scope requested: " +item);
           }
       }

       if (studyUID == null && seriesUID == null) {
           return null;
       } else if (studyUID == null) {
           LOG.info("no StudyInstanceUID provided in the scope");
           errorResponse.errorDescription = "no StudyInstanceUID provided in the scope";
           throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build());
       } else if (seriesUID == null) {
           errorResponse.errorDescription = "no StudyInstanceUID provided in the scope";
           LOG.info("no SeriesInstanceUID provided in the scope");
           errorResponse.errorDescription = "no SeriesInstanceUID provided in the scope";
           throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build());
       } else {
           return new UIDPair(studyUID, seriesUID);
       }
    }
}

