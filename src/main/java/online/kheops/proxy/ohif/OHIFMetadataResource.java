package online.kheops.proxy.ohif;

import online.kheops.proxy.marshaller.JSONAttributesListMarshaller;
import online.kheops.proxy.tokens.AuthorizationToken;
import org.dcm4che3.data.Attributes;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.BAD_GATEWAY;
import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_JSON;

@Path("/")
public class OHIFMetadataResource {
    private static final Logger LOG = Logger.getLogger(OHIFMetadataResource.class.getName());

    private static final String LINK_AUTHORIZATION = "X-Link-Authorization";
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    private static final Client CLIENT = ClientBuilder.newClient()
            .register(JSONAttributesListMarshaller.class)
            .register(MoxyJsonFeature.class);

    @Context
    ServletContext context;

    @HeaderParam(LINK_AUTHORIZATION)
    String linkAuthorizationHeader;

    @HeaderParam(HEADER_X_FORWARDED_FOR)
    String headerXForwardedFor;

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/ohifmetadata")
    public MetadataDTO wado(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                            @PathParam("studyInstanceUID") String studyInstanceUID,
                            @QueryParam("firstseries") String firstSeriesInstanceUID,
                            @QueryParam("inbox") Boolean inbox,
                            @QueryParam("album") String album) {
        return ohifMetadata(studyInstanceUID, firstSeriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader), inbox, album);
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{capability:[a-zA-Z0-9]{22}}/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/ohifmetadata")
    public MetadataDTO wadoWithCapability(@PathParam("capability") String capabilityToken,
                                          @PathParam("studyInstanceUID") String studyInstanceUID,
                                          @QueryParam("firstseries") String firstSeriesInstanceUID,
                                          @QueryParam("inbox") Boolean inbox,
                                          @QueryParam("album") String album) {
        return ohifMetadata(studyInstanceUID, firstSeriesInstanceUID, AuthorizationToken.from(capabilityToken), inbox, album);
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("password/dicomweb/ohifservermetadata")
    public String ohifServerMetadataResource(@HeaderParam(AUTHORIZATION) String authorizationHeader) {
        return ohifServerMetadata(AuthorizationToken.fromAuthorizationHeader(authorizationHeader));
    }

    private String ohifServerMetadata(AuthorizationToken authorizationToken) {

        final boolean linkAuthorization = linkAuthorizationHeader != null && linkAuthorizationHeader.equalsIgnoreCase("true");

        final URI rootURI = getParameterURI("online.kheops.root.uri");
        final URI dicomWebURI;
        if (linkAuthorization) {
            dicomWebURI = UriBuilder.fromUri(rootURI).path("/api/link/" + authorizationToken.getToken()).build();
        } else {
            dicomWebURI = UriBuilder.fromUri(rootURI).path("/api").build();
        }

        return String.format("{\n" +
                "   \"transactionId\":\"testDICOMs\",\n" +
                "   \"servers\": {\n" +
                "      \"dicomWeb\": [\n" +
                "         {\n" +
                "            \"name\": \"DCM4CHEE\",\n" +
                "            \"wadoUriRoot\": \"%s/wado\",\n" +
                "            \"qidoRoot\": \"%s\",\n" +
                "            \"wadoRoot\": \"%s\",\n" +
                "            \"qidoSupportsIncludeField\": true,\n" +
                "            \"imageRendering\": \"wadors\",\n" +
                "            \"thumbnailRendering\": \"wadors\",\n" +
                "            \"enableStudyLazyLoad\": true\n" +
                "         }\n" +
                "      ]\n" +
                "   }\n" +
                "}\n" +
                "\n", dicomWebURI, dicomWebURI, dicomWebURI);
    }

    private MetadataDTO ohifMetadata(String studyInstanceUID, String firstSeriesInstanceUID, AuthorizationToken authorizationToken, Boolean inbox, String album) {
        final URI authorizationServerURI = getParameterURI("online.kheops.auth_server.uri");
        final URI rootURI = getParameterURI("online.kheops.root.uri");

        final boolean linkAuthorization = linkAuthorizationHeader != null && linkAuthorizationHeader.equalsIgnoreCase("true");

        final URI wadoUri;
        if (linkAuthorization) {
            wadoUri = UriBuilder.fromUri(rootURI).path("/api/link/" + authorizationToken.getToken() + "/wado").build();
        } else {
            wadoUri = UriBuilder.fromUri(rootURI).path("/api/wado").build();
        }

        final UriBuilder metadataServiceUriBuilder = UriBuilder.fromUri(authorizationServerURI).path("/studies/{StudyInstanceUID}/metadata");

        if (inbox != null && inbox) {
            metadataServiceUriBuilder.queryParam("inbox", "true");
        }
        if (album != null && !album.isEmpty()) {
            metadataServiceUriBuilder.queryParam("album", album);
        }

        final URI metadataServiceURI = metadataServiceUriBuilder.build(studyInstanceUID);

        try {
            return MetadataDTO.from(wadoUri, firstSeriesInstanceUID,
                    CLIENT.target(metadataServiceURI)
                            .request(APPLICATION_DICOM_JSON)
                            .header(AUTHORIZATION, "Bearer " + authorizationToken)
                            .header(HEADER_X_FORWARDED_FOR, headerXForwardedFor)
                            .get(new GenericType<List<Attributes>>() {}));
        } catch (ResponseProcessingException | WebApplicationException e) {
            final int status;
            if (e instanceof ResponseProcessingException) {
                status = ((ResponseProcessingException) e).getResponse().getStatus();
            } else {
                status = ((WebApplicationException) e).getResponse().getStatus();
            }
            if (status == UNAUTHORIZED.getStatusCode()) {
                LOG.log(WARNING, "Unauthorized", e);
                throw new WebApplicationException(UNAUTHORIZED);
            } else if (status == NOT_FOUND.getStatusCode()) {
                LOG.log(WARNING, "Metadata not found", e);
                throw new WebApplicationException(NOT_FOUND);
            } else if (status == BAD_REQUEST.getStatusCode()) {
                LOG.log(WARNING, "Bad Request", e);
                throw new WebApplicationException(BAD_REQUEST);
            } else {
                LOG.log(SEVERE, "Bad Gateway", e);
                throw new WebApplicationException(BAD_GATEWAY);
            }
        } catch (ProcessingException e) {
            LOG.log(SEVERE, "Processing Error", e);
            throw new WebApplicationException(BAD_GATEWAY);
        }
    }

    private URI getParameterURI(String parameter) {
        try {
            return new URI(context.getInitParameter(parameter));
        } catch (URISyntaxException e) {
            LOG.log(SEVERE, "Error with the STOWServiceURI", e);
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }
    }
}
