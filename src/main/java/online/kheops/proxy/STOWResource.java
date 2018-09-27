package online.kheops.proxy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.dcm4che3.data.Attributes;
import org.weasis.dicom.web.StowRS;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public final class STOWResource {
    private static final Logger LOG = Logger.getLogger(STOWResource.class.getName());

    @Context
    ServletContext context;

    @HeaderParam("Content-Type")
    MediaType contentType;

    @POST
    @Path("/password/dicomweb/studies")
    @Consumes("multipart/related")
    @Produces({"application/dicom+json; qs=0.9, application/dicom+xml; qs=1"})
    public Response stow(InputStream inputStream, @HeaderParam("Authorization") String authorizationHeader, @QueryParam("album") String albumId) {
        return store(inputStream, authorizationHeaderToToken(authorizationHeader), albumId, null);
    }

    @POST
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies")
    @Consumes("multipart/related")
    @Produces({"application/dicom+json; qs=0.9, application/dicom+xml; qs=1"})
    public Response stowWithCapability(InputStream inputStream, @PathParam("capability") String capabilityToken, @QueryParam("album") String albumId) {
        return store(inputStream, capabilityToken, albumId, null);
    }

    @POST
    @Path("/password/dicomweb/studies/{studyInstanceUID}")
    @Consumes("multipart/related")
    @Produces({"application/dicom+json; qs=0.9, application/dicom+xml; qs=1"})
    public Response stowStudy(InputStream inputStream, @HeaderParam("Authorization") String authorizationHeader, @PathParam("studyInstanceUID") String studyInstanceUID, @QueryParam("album") String albumId) {
        return store(inputStream, authorizationHeaderToToken(authorizationHeader), albumId, studyInstanceUID);
    }

    @POST
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies/{studyInstanceUID}")
    @Consumes("multipart/related")
    @Produces({"application/dicom+json; qs=0.9, application/dicom+xml; qs=1"})
    public Response stowStudyWithCapability(InputStream inputStream, @PathParam("capability") String capabilityToken, @PathParam("studyInstanceUID") String studyInstanceUID, @QueryParam("album") String albumId) {
        return store(inputStream, capabilityToken, albumId, studyInstanceUID);
    }

    private Response store(InputStream inputStream, String bearerToken, String albumId, String studyInstanceUID) {
        final URI STOWServiceURI = getParameterURI("online.kheops.pacs.uri");
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");

        try (StowRS stowRS = new StowRS(STOWServiceURI.toString() + "/studies", getStowContentType(), null, "Bearer " + getPostBearerToken())) {
            STOWService stowService = new STOWService(stowRS);
            return new STOWProxy(contentType, inputStream, stowService, new AuthorizationManager(authorizationURI, bearerToken, albumId, studyInstanceUID)).getResponse();
        } catch (STOWGatewayException e) {
            LOG.log(Level.SEVERE, "Gateway Error", e);
            throw new WebApplicationException(Response.Status.BAD_GATEWAY);
        } catch (STOWRequestException e) {
            LOG.log(Level.WARNING, "Bad request Error", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in the proxy", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private StowRS.ContentType getStowContentType() {
        try {
            return StowRS.ContentType.from(contentType.getParameters().get("type"));
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Bad request Error", e);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private URI getParameterURI(String parameter) {
        try {
            return new URI(context.getInitParameter(parameter));
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "Error with the STOWServiceURI", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String getPostBearerToken() {
        final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecretpost");
        final Algorithm algorithmHMAC;
        try {
            algorithmHMAC = Algorithm.HMAC256(authSecret);
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "online.kheops.auth.hmacsecretpost is not a valid HMAC secret", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        JWTCreator.Builder jwtBuilder = JWT.create()
                .withIssuer("auth.kheops.online")
                .withAudience("dicom.kheops.online")
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));

        return jwtBuilder.sign(algorithmHMAC);
    }

    private String authorizationHeaderToToken(String authorizationHeader) {
        final String token;
        if (authorizationHeader != null) {

            if (authorizationHeader.toUpperCase().startsWith("BASIC ")) {
                final String encodedAuthorization = authorizationHeader.substring(6);

                final String decoded = new String(Base64.getDecoder().decode(encodedAuthorization), StandardCharsets.UTF_8);
                String[] split = decoded.split(":");
                if (split.length != 2) {
                    LOG.log(Level.WARNING, "Basic authentication doesn't have a username and password");
                    throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
                }

                token = split[1];
            } else if (authorizationHeader.toUpperCase().startsWith("BEARER ")) {
                token = authorizationHeader.substring(7);
            } else {
                LOG.log(Level.WARNING, "Unknown authorization header");
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
            }

            if (token.length() == 0) {
                LOG.log(Level.WARNING, "Empty authorization token");
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } else {
            LOG.log(Level.WARNING, "Missing authorization header");
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        return token;
    }
}