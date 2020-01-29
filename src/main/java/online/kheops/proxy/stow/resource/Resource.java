package online.kheops.proxy.stow.resource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.proxy.multipart.MultipartStreamingOutput;
import online.kheops.proxy.multipart.MultipartStreamingWriter;
import online.kheops.proxy.stow.FetchRequester;
import online.kheops.proxy.stow.GatewayException;
import online.kheops.proxy.stow.Proxy;
import online.kheops.proxy.stow.RequestException;
import online.kheops.proxy.stow.authorization.AuthorizationManager;
import online.kheops.proxy.tokens.AccessTokenException;
import online.kheops.proxy.tokens.AuthorizationToken;
import online.kheops.proxy.tokens.Introspect;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.ws.rs.MediaTypes;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_TYPE;
import static org.dcm4che3.ws.rs.MediaTypes.MULTIPART_RELATED_APPLICATION_DICOM_TYPE;
import static org.glassfish.jersey.client.ClientProperties.REQUEST_ENTITY_PROCESSING;
import static org.glassfish.jersey.client.RequestEntityProcessing.CHUNKED;

@Path("/")
public final class Resource {
    private static final Logger LOG = Logger.getLogger(Resource.class.getName());
    private static final Client CLIENT = newClient();

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_LINK_AUTHORIZATION = "X-Link-Authorization";

    private static final String BOUNDARY = "Boundary-ffc9be9e668952f2e1815be2709b87827169798a";

    private static Client newClient() {
        final Client client = ClientBuilder.newClient();
        client.register(MultipartStreamingWriter.class);
        client.property(REQUEST_ENTITY_PROCESSING, CHUNKED);
        return client;
    }

    @Context
    ServletContext context;

    @Context
    Providers providers;

    @HeaderParam(CONTENT_TYPE)
    MediaType contentType;

    @HeaderParam(HEADER_X_FORWARDED_FOR)
    String headerXForwardedFor;

    @HeaderParam(HEADER_X_LINK_AUTHORIZATION)
    String headerXLinkAuthorization;

    @Context
    HttpServletRequest request;

    @POST
    @Path("/password/dicomweb/studies")
    @Consumes("multipart/related,multipart/form-data,application/dicom")
    @Produces({"application/dicom+json;qs=0.9, application/dicom+xml;qs=1.0, application/json;qs=0.8"})
    public Response stow(@HeaderParam("Authorization") String authorizationHeader, @QueryParam("album") String albumId) {
        return store(AuthorizationToken.fromAuthorizationHeader(authorizationHeader), albumId, null);
    }

    @POST
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies")
    @Consumes("multipart/related,multipart/form-data,application/dicom")
    @Produces({"application/dicom+json;qs=0.9, application/dicom+xml;qs=1.0, application/json;qs=0.8"})
    public Response stowWithCapability(@PathParam("capability") String capabilityToken, @QueryParam("album") String albumId) {
        return store(AuthorizationToken.from(capabilityToken), albumId, null);
    }

    @POST
    @Path("/password/dicomweb/studies/{studyInstanceUID}")
    @Consumes("multipart/related,multipart/form-data,application/dicom")
    @Produces({"application/dicom+json;qs=0.9, application/dicom+xml;qs=1.0, application/json;qs=0.8"})
    public Response stowStudy(@HeaderParam("Authorization") String authorizationHeader, @PathParam("studyInstanceUID") String studyInstanceUID, @QueryParam("album") String albumId) {
        return store(AuthorizationToken.fromAuthorizationHeader(authorizationHeader), albumId, studyInstanceUID);
    }

    @POST
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies/{studyInstanceUID}")
    @Consumes("multipart/related,multipart/form-data,application/dicom")
    @Produces({"application/dicom+json;qs=0.9, application/dicom+xml;qs=1.0, application/json;qs=0.8"})
    public Response stowStudyWithCapability(@PathParam("capability") String capabilityToken, @PathParam("studyInstanceUID") String studyInstanceUID, @QueryParam("album") String albumId) {
        return store(AuthorizationToken.from(capabilityToken), albumId, studyInstanceUID);
    }

    private Response store(AuthorizationToken authorizationToken, String albumId, String studyInstanceUID) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");

        final URI introspectionURI = UriBuilder.fromUri(authorizationURI).path("/token/introspect").build();
        final Introspect.Response introspectResponse;
        try {
            introspectResponse = Introspect.endpoint(context, introspectionURI, headerXForwardedFor).token(authorizationToken.getToken());
            if (!introspectResponse.isActive()) {
                LOG.log(Level.WARNING, "Authorization token is not valid for writing");
                throw new NotAuthorizedException("Bearer", "Basic");
            }
            if (!introspectResponse.isValidForScope("write")) {
                LOG.log(Level.WARNING, "Authorization token is not valid for writing");
                throw new WebApplicationException(Response.status(FORBIDDEN).entity("Authorization is not valid for posting").build());
            }
        } catch (AccessTokenException e) {
            LOG.log(Level.SEVERE, "Unable to introspect the token", e);
            throw new WebApplicationException(Response.status(BAD_GATEWAY).build());
        }

        final FetchRequester fetchRequester = FetchRequester.newFetchRequester(authorizationURI, authorizationToken, albumId);
        final AuthorizationManager authorizationManager = new AuthorizationManager(authorizationURI, authorizationToken, albumId, headerXLinkAuthorization);

        try (InputStream inputStream = getConvertedInputStream(request.getInputStream())) {
            final Proxy proxy = new Proxy(providers, getConvertedContentType(), inputStream, authorizationManager, fetchRequester::addSeries);
            return processProxy(proxy, authorizationManager, studyInstanceUID, introspectResponse);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "", e);
            throw new WebApplicationException(BAD_REQUEST);
        } finally {
            fetchRequester.fetch();
        }
    }

    private InputStream getConvertedInputStream(final InputStream inputStream) {
        if (contentType.isCompatible(APPLICATION_DICOM_TYPE)) {
            final List<InputStream> streams = new ArrayList<>(3);
            streams.add(new ByteArrayInputStream(("\r\n--" + BOUNDARY + "\r\nContent-Type: application/dicom\r\n\r\n").getBytes(US_ASCII)));
            streams.add(inputStream);
            streams.add(new ByteArrayInputStream(("\r\n--" + BOUNDARY + "--").getBytes(US_ASCII)));

            return new SequenceInputStream(Collections.enumeration(streams));
        } else {
            return inputStream;
        }
    }

    private MediaType getConvertedContentType() {
        if (contentType.isCompatible(APPLICATION_DICOM_TYPE)) {
            Map<String, String > parameters = new HashMap<>(2);
            parameters.put("type", "\"application/dicom\"");
            parameters.put("boundary", BOUNDARY);

            return new MediaType("multipart", "related", parameters);
        } else {
            return contentType;
        }
    }

    private MediaType getGatewayContentType() {
        MediaType filteredContentType = getConvertedContentType();
        if (filteredContentType.isCompatible(MULTIPART_FORM_DATA_TYPE)) {
            return MULTIPART_RELATED_APPLICATION_DICOM_TYPE;
        } else {
            return filteredContentType;
        }
    }

    private Response processProxy(Proxy proxy, AuthorizationManager authorizationManager, String studyInstanceUID, Introspect.Response introspectResponse) {
        URI stowServiceURI = getParameterURI("online.kheops.pacs.uri");

        if (studyInstanceUID != null) {
            stowServiceURI = UriBuilder.fromUri(stowServiceURI).path("/studies/{StudyInstanceUID}").build(studyInstanceUID);
        } else {
            stowServiceURI = UriBuilder.fromUri(stowServiceURI).path("/studies").build();
        }

        MultipartStreamingOutput multipartStreamingOutput = output -> {
            try {
                proxy.processStream(output);
            } catch (GatewayException e) {
                LOG.log(Level.SEVERE, "Gateway Error", e);
                throw new WebApplicationException(BAD_GATEWAY);
            } catch (RequestException e) {
                LOG.log(Level.WARNING, "Bad request Error", e);
                throw new WebApplicationException(BAD_REQUEST);
            } catch (WebApplicationException e) {
                throw e;
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error in the proxy", e);
                throw new WebApplicationException(INTERNAL_SERVER_ERROR);
            }
        };

        try (final Response gatewayResponse = CLIENT.target(stowServiceURI)
                    .request()
                    .header(AUTHORIZATION, "Bearer " + getPostBearerToken(introspectResponse))
                    .header(ACCEPT, MediaTypes.APPLICATION_DICOM_XML)
                    .post(Entity.entity(multipartStreamingOutput, getGatewayContentType()));
             final InputStream responseStream = gatewayResponse.readEntity(InputStream.class)) {
            if (gatewayResponse.getStatusInfo().getFamily() != SUCCESSFUL && gatewayResponse.getStatus() != CONFLICT.getStatusCode()) {
                LOG.log(Level.SEVERE, () -> "Gateway response was unsuccessful, Status: " + gatewayResponse.getStatus());
                try {
                    String responseString = gatewayResponse.readEntity(String.class);
                    LOG.log(Level.SEVERE, () -> "Response Content: " + responseString);
                } catch (ProcessingException pe) {
                    LOG.log(Level.SEVERE, "Unable to get a response content", pe);
                }

                throw new WebApplicationException(BAD_GATEWAY);
            }

            return authorizationManager.getResponse(SAXReader.parse(responseStream), gatewayResponse.getStatus());
        } catch (ProcessingException e) {
            LOG.log(Level.SEVERE, "Gateway Processing Error", e);
            if (e.getCause() instanceof WebApplicationException) {
                WebApplicationException cause = (WebApplicationException)e.getCause();
                throw new WebApplicationException(cause.getResponse().getStatus());
            } else {
                throw new InternalServerErrorException(e);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.log(Level.WARNING, "Error parsing response", e);
            throw new WebApplicationException(BAD_GATEWAY);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "error", e);
            throw new WebApplicationException(BAD_GATEWAY);
        }
    }

    private URI getParameterURI(String parameter) {
        try {
            return new URI(context.getInitParameter(parameter));
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "Error with the STOWServiceURI", e);
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }
    }

    private String getPostBearerToken(Introspect.Response introspectResponse) {
        final String authSecret = context.getInitParameter("online.kheops.auth.hmacsecretpost");
        final Algorithm algorithmHMAC = Algorithm.HMAC256(authSecret);

        JWTCreator.Builder jwtBuilder = JWT.create()
                .withIssuer("auth.kheops.online")
                .withAudience("dicom.kheops.online")
                .withSubject(introspectResponse.getSubject())
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));

        introspectResponse.getActingParty().ifPresent(actingParty -> jwtBuilder.withClaim("act", actingParty));
        introspectResponse.getAuthorizedParty().ifPresent(authorizedParty -> jwtBuilder.withClaim("azp", authorizedParty));
        introspectResponse.getCapabilityTokenId().ifPresent(capabilityTokenId -> jwtBuilder.withClaim("cap_token", capabilityTokenId));

        return jwtBuilder.sign(algorithmHMAC);
    }
}
