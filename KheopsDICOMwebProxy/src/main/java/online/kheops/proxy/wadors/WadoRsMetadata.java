package online.kheops.proxy.wadors;

import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.marshaller.AttributesStreamingOutput;
import online.kheops.proxy.tokens.AccessToken;
import online.kheops.proxy.tokens.AccessTokenException;
import online.kheops.proxy.tokens.AuthorizationToken;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.VR;
import org.dcm4che3.json.JSONReader;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_JSON;

@Path("/")
public final class WadoRsMetadata {
    private static final Logger LOG = Logger.getLogger(WadoRsMetadata.class.getName());

    private static final Client CLIENT = ClientBuilder.newClient();

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String LINK_AUTHORIZATION = "X-Link-Authorization";

    private static final Pattern SERIES_URL_PATTERN = Pattern.compile("(studies/(?:(?:[0-9]+[.])*[0-9]+)/series.*)");

    @Context
    UriInfo uriInfo;

    @Context
    ServletContext context;

    @HeaderParam(ACCEPT)
    String acceptParam;

    @HeaderParam(ACCEPT_CHARSET)
    String acceptCharsetParam;

    @HeaderParam(HEADER_X_FORWARDED_FOR)
    String headerXForwardedFor;

    @GET
    @Produces(APPLICATION_DICOM_JSON + ";qs=1," + APPLICATION_JSON + ";qs=0.8," + "multipart/related;type=\"application/dicom+xml\";qs=0.9")
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/metadata")
    public Response wadoSeriesMetadata(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                                       @PathParam("studyInstanceUID") String studyInstanceUID,
                                       @PathParam("seriesInstanceUID") String seriesInstanceUID,
                                       @HeaderParam(LINK_AUTHORIZATION) String linkAuthorizationHeader) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader), linkAuthorizationHeader);
    }

    @GET
    @Produces(APPLICATION_DICOM_JSON + ";qs=1," + APPLICATION_JSON + ";qs=0.8," + "multipart/related;type=\"application/dicom+xml\";qs=0.9")
    @Path("/password/dicomweb/studies/{studyInstanceUID:([0-9]+[.])*[0-9]+}/series/{seriesInstanceUID:([0-9]+[.])*[0-9]+}/instances/{sopInstanceUID:([0-9]+[.])*[0-9]+}/metadata")
    public Response wadoInstanceMetadata(@HeaderParam(AUTHORIZATION) String authorizationHeader,
                                         @PathParam("studyInstanceUID") String studyInstanceUID,
                                         @PathParam("seriesInstanceUID") String seriesInstanceUID,
                                         @HeaderParam(LINK_AUTHORIZATION) String linkAuthorizationHeader) {
        return webAccess(studyInstanceUID, seriesInstanceUID, AuthorizationToken.fromAuthorizationHeader(authorizationHeader), linkAuthorizationHeader);
    }

    private Response webAccess(String studyInstanceUID, String seriesInstanceUID, AuthorizationToken authorizationToken, String linkAuthorizationHeader) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        URI wadoServiceURI = getParameterURI("online.kheops.pacs.uri");
        final URI rootURI = getParameterURI("online.kheops.root.uri");
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        final boolean linkAuthorization = linkAuthorizationHeader != null && linkAuthorizationHeader.equalsIgnoreCase("true");

        final AccessToken accessToken;
        try {
            accessToken = AccessToken.createBuilder(authorizationURI)
                    .withClientId(System.getProperty("online.kheops.client.dicomwebproxyclientid"))
                    .withClientSecret(System.getenv("DICOMWEB_PROXY_SECRET"))
                    .withCapability(authorizationToken.getToken())
                    .withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID))
                    .xForwardedFor(headerXForwardedFor)
                    .build();
        } catch (AccessTokenException e) {
            LOG.log(WARNING, "Unable to get an access token", e);
            throw new NotAuthorizedException("Bearer", "Basic");
        }

        final Matcher matcher = SERIES_URL_PATTERN.matcher(uriInfo.getPath());

        if (!matcher.find()) {
            LOG.log(SEVERE, "could not find URL series subpath");
            throw new NotFoundException();
        }

        WebTarget webTarget = CLIENT.target(wadoServiceURI).path(matcher.group(1));
        for (Map.Entry<String, List<String>> parameter: queryParameters.entrySet()) {
            if (!parameter.getKey().equals(ACCEPT)) {
                webTarget = webTarget.queryParam(parameter.getKey(), parameter.getValue().toArray());
            }
        }

        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header(AUTHORIZATION, accessToken.getHeaderValue());
        if (acceptParam != null) {
            invocationBuilder.accept(APPLICATION_DICOM_JSON);
        }
        if (acceptCharsetParam != null) {
            invocationBuilder.header(ACCEPT_CHARSET, acceptCharsetParam);
        }

        final String dicomwebRoot;
        if (linkAuthorization) {
            dicomwebRoot = rootURI + "/api/link/" + authorizationToken + "/";
        } else {
            dicomwebRoot = rootURI.toString() + "/api/";
        }

        final Response upstreamResponse;
        try {
            upstreamResponse = invocationBuilder.get();
        } catch (ProcessingException e) {
            LOG.log(SEVERE, "error processing response from upstream", e);
            throw new WebApplicationException(BAD_GATEWAY);
        }

        if (upstreamResponse.getStatusInfo().getFamily() != SUCCESSFUL) {
            LOG.log(WARNING, () -> "upstreamResponse was not successful (" + upstreamResponse.getStatus() + ")");
            try {
                String entity = upstreamResponse.readEntity(String.class);
                throw new WebApplicationException(Response.status(upstreamResponse.getStatus())
                        .entity(entity)
                        .build());
            } catch (ProcessingException e) {
                LOG.log(SEVERE, "error getting the entity from upstream", e);
                throw new ServerErrorException(BAD_GATEWAY);
            } finally {
                upstreamResponse.close();
            }
        }

        AttributesStreamingOutput streamingOutput = output -> {
            try (final InputStream inputStream = upstreamResponse.readEntity(InputStream.class);
                 final JsonParser parser = Json.createParser(inputStream)) {

                final JSONReader jsonReader = new JSONReader(parser);

                jsonReader.readDatasets((fmi, dataset) -> {
                    try {
                        dataset.accept(new BulkDataVisitor(dicomwebRoot), true);
                        output.write(dataset);
                    } catch (Exception e) {
                        throw new AcceptException(e);
                    }
                });
            } catch (AcceptException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                } else {
                    throw new IOException(e.getCause());
                }
            } finally {
                upstreamResponse.close();
            }
        };

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);

        return Response.status(upstreamResponse.getStatus())
                .entity(streamingOutput)
                .cacheControl(cacheControl)
                .build();
    }

    private URI getParameterURI(String parameter) {
        try {
            return new URI(System.getProperty(parameter));
        } catch (URISyntaxException e) {
            LOG.log(SEVERE, e, () -> "Error getting " + parameter);
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }
    }

    private static class BulkDataVisitor implements Attributes.Visitor {
        final String dicomwebRoot;

        private BulkDataVisitor(String dicomwebRoot) {
            this.dicomwebRoot = dicomwebRoot;
        }

        public boolean visit(Attributes attrs, int tag, VR vr, Object value) {

            if (value instanceof BulkData) {
                BulkData bulkData = (BulkData)value;
                final String uri = bulkData.getURI();
                if (uri != null) {
                    final Matcher matcher = SERIES_URL_PATTERN.matcher(uri);
                    if (matcher.find()) {
                        bulkData.setURI(dicomwebRoot + matcher.group(1));
                    }
                }
            }
            return true;
        }
    }

    // class that is used because accept() throws Exception and the readDatasets callback can not throw exceptions...
    private static class AcceptException extends RuntimeException {
        AcceptException(Exception e) {
            super(e);
        }
    }
}
