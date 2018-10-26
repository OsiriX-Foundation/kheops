package online.kheops.proxy.stow.resource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.proxy.multipart.MultipartStreamingOutput;
import online.kheops.proxy.multipart.MultipartStreamingWriter;
import online.kheops.proxy.stow.GatewayException;
import online.kheops.proxy.stow.Proxy;
import online.kheops.proxy.stow.RequestException;
import online.kheops.proxy.stow.authorization.AuthorizationManager;
import online.kheops.proxy.tokens.AccessToken;
import online.kheops.proxy.tokens.AccessTokenException;
import online.kheops.proxy.tokens.AuthorizationToken;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.ws.rs.MediaTypes;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.glassfish.jersey.client.ClientProperties.REQUEST_ENTITY_PROCESSING;
import static org.glassfish.jersey.client.RequestEntityProcessing.CHUNKED;

@Path("/")
public final class Resource {
    private static final Logger LOG = Logger.getLogger(Resource.class.getName());
    private static final Client CLIENT = newClient();

    private static Client newClient() {
        final Client client = ClientBuilder.newClient();
        client.register(MultipartStreamingWriter.class);
        client.property(REQUEST_ENTITY_PROCESSING, CHUNKED);
        return client;
    }

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
        URI stowServiceURI = getParameterURI("online.kheops.pacs.uri");

        try {
            AccessToken.createBuilder(authorizationURI)
                    .withCapability(authorizationToken.getToken())
                    .build();
        } catch (AccessTokenException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        if (studyInstanceUID != null) {
            stowServiceURI = UriBuilder.fromUri(stowServiceURI).path("/studies/{StudyInstanceUID}").build(studyInstanceUID);
        } else {
            stowServiceURI = UriBuilder.fromUri(stowServiceURI).path("/studies").build();
        }

        AuthorizationManager authorizationManager = new AuthorizationManager(authorizationURI, authorizationToken, albumId, studyInstanceUID);

        MultipartStreamingOutput multipartStreamingOutput = output -> {
            try {
                new Proxy(contentType, inputStream, output, authorizationManager);
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
        };

        try {
            InputStream responseStream = CLIENT.target(stowServiceURI)
                    .request()
                    .header("Authorization", "Bearer " + getPostBearerToken())
                    .header("Accept", MediaTypes.APPLICATION_DICOM_XML)
                    .post(Entity.entity(multipartStreamingOutput, contentType), InputStream.class);

            return authorizationManager.getResponse(SAXReader.parse(responseStream));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.log(Level.WARNING, "Error parsing response", e);
            throw new WebApplicationException(Response.Status.BAD_GATEWAY);
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
