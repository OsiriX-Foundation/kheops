package online.kheops.proxy.stow;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.tokens.AccessToken;
import online.kheops.proxy.tokens.AccessTokenException;
import online.kheops.proxy.tokens.AuthorizationToken;
import org.weasis.dicom.web.StowRS;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/")
public final class Resource {
    private static final Logger LOG = Logger.getLogger(Resource.class.getName());

    @Context
    ServletContext context;

    @HeaderParam("Content-Type")
    MediaType contentType;

    @POST
    @Path("/password/dicomweb/studies")
    @Consumes("multipart/related")
    @Produces({"application/dicom+json; qs=0.9, application/dicom+xml; qs=1"})
    public Response stow(InputStream inputStream, @HeaderParam("Authorization") String authorizationHeader, @QueryParam("album") String albumId) {
        return store(inputStream, AuthorizationToken.fromAuthorizationHeader(authorizationHeader), albumId, null);
    }

    @POST
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies")
    @Consumes("multipart/related")
    @Produces({"application/dicom+json; qs=0.9, application/dicom+xml; qs=1"})
    public Response stowWithCapability(InputStream inputStream, @PathParam("capability") String capabilityToken, @QueryParam("album") String albumId) {
        return store(inputStream, AuthorizationToken.from(capabilityToken), albumId, null);
    }

    @POST
    @Path("/password/dicomweb/studies/{studyInstanceUID}")
    @Consumes("multipart/related")
    @Produces({"application/dicom+json; qs=0.9, application/dicom+xml; qs=1"})
    public Response stowStudy(InputStream inputStream, @HeaderParam("Authorization") String authorizationHeader, @PathParam("studyInstanceUID") String studyInstanceUID, @QueryParam("album") String albumId) {
        return store(inputStream, AuthorizationToken.fromAuthorizationHeader(authorizationHeader), albumId, studyInstanceUID);
    }

    @POST
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies/{studyInstanceUID}")
    @Consumes("multipart/related")
    @Produces({"application/dicom+json; qs=0.9, application/dicom+xml; qs=1"})
    public Response stowStudyWithCapability(InputStream inputStream, @PathParam("capability") String capabilityToken, @PathParam("studyInstanceUID") String studyInstanceUID, @QueryParam("album") String albumId) {
        return store(inputStream, AuthorizationToken.from(capabilityToken), albumId, studyInstanceUID);
    }

    private Response store(InputStream inputStream, AuthorizationToken authorizationToken, String albumId, String studyInstanceUID) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        URI STOWServiceURI = getParameterURI("online.kheops.pacs.uri");

        try {
            AccessToken.createBuilder(authorizationURI)
                    .withCapability(authorizationToken.getToken())
                    .build();
        } catch (AccessTokenException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        if (studyInstanceUID != null) {
            STOWServiceURI = UriBuilder.fromUri(STOWServiceURI).path("/studies/{StudyInstanceUID}").build(studyInstanceUID);
        } else {
            STOWServiceURI = UriBuilder.fromUri(STOWServiceURI).path("/studies").build();
        }

        try (StowRS stowRS = new StowRS(STOWServiceURI.toString(), getStowContentType(), null, "Bearer " + getPostBearerToken())) {
            Service stowService = new Service(stowRS);
            return new Proxy(contentType, inputStream, stowService, new AuthorizationManager(authorizationURI, authorizationToken, albumId, studyInstanceUID)).getResponse();
        } catch (GatewayException e) {
            LOG.log(Level.SEVERE, "Gateway Error", e);
            throw new WebApplicationException(Response.Status.BAD_GATEWAY);
        } catch (RequestException e) {
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
}