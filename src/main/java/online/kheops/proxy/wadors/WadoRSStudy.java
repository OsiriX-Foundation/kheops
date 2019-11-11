package online.kheops.proxy.wadors;

import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.marshaller.JSONAttributesListMarshaller;
import online.kheops.proxy.marshaller.JSONAttributesWriter;
import online.kheops.proxy.multipart.MultipartStreamingOutput;
import online.kheops.proxy.multipart.MultipartStreamingWriter;
import online.kheops.proxy.multipart.StreamingBodyPart;
import online.kheops.proxy.tokens.AccessToken;
import online.kheops.proxy.tokens.AccessTokenException;
import online.kheops.proxy.tokens.AuthorizationToken;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.mime.MultipartParser;
import org.dcm4che3.ws.rs.MediaTypes;

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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static org.glassfish.jersey.media.multipart.Boundary.BOUNDARY_PARAMETER;

@Path("/")
public final class WadoRSStudy {
    private static final Logger LOG = Logger.getLogger(WadoRSStudy.class.getName());
    private static final Client CLIENT = newClient();

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    private static Client newClient() {
        final Client client = ClientBuilder.newClient();
        client.register(MultipartStreamingWriter.class);
        client.register(JSONAttributesWriter.class);
        client.register(JSONAttributesListMarshaller.class);
        return client;
    }

    @Context
    ServletContext context;

    @HeaderParam(ACCEPT)
    String acceptParam;

    @HeaderParam(ACCEPT_CHARSET)
    String acceptCharsetParam;

    @HeaderParam(AUTHORIZATION)
    String authorizationHeader;

    @HeaderParam(HEADER_X_FORWARDED_FOR)
    String headerXForwardedFor;


    @GET
    @Path("password/dicomweb/studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("multipart/related")
    public Response wado(@PathParam("StudyInstanceUID") String studyInstanceUID,
                         @QueryParam("album") String fromAlbumPk,
                         @QueryParam("inbox") Boolean fromInbox) {

        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        final URI wadoServiceURI = getParameterURI("online.kheops.pacs.uri");

        final AuthorizationToken authorizationToken = AuthorizationToken.fromAuthorizationHeader(authorizationHeader);
        final AccessToken.AccessTokenBuilder accessTokenBuilder =  AccessToken.createBuilder(authorizationURI)
                .withClientId(context.getInitParameter("online.kheops.client.dicomwebproxyclientid"))
                .withClientSecret(context.getInitParameter("online.kheops.client.dicomwebproxysecret"))
                .withCapability(authorizationToken.getToken());

        final UriBuilder qidoServiceURIBuilder = UriBuilder.fromUri(authorizationURI)
                .path("/studies/{StudyInstanceUID}/series");

        if (fromAlbumPk != null) {
            qidoServiceURIBuilder.queryParam("album", fromAlbumPk);
        }
        if (fromInbox != null) {
            qidoServiceURIBuilder.queryParam("inbox", fromInbox);
        }

        final MediaType acceptMediaType;
        try {
            // TODO this is not quite right, because the accept header might have multiple types
            acceptMediaType = MediaType.valueOf(acceptParam);
        } catch (IllegalArgumentException e) {
            LOG.log(WARNING, "Error while getting the accept media type", e);
            throw new WebApplicationException(NOT_ACCEPTABLE);

        }
        final MediaType requestAcceptType;
        if (acceptMediaType.isWildcardType()) {
            requestAcceptType = new MediaType("multipart", "related", Collections.singletonMap("type", "application/octet-stream"));
        } else {
            requestAcceptType = acceptMediaType;
        }

        final URI qidoServiceURI = qidoServiceURIBuilder.build(studyInstanceUID);

        final List<Attributes> seriesList;
        try (final Response listResponse = CLIENT.target(qidoServiceURI).request(MediaTypes.APPLICATION_DICOM_JSON_TYPE)
                    .header(AUTHORIZATION, authorizationHeader)
                    .header(HEADER_X_FORWARDED_FOR, headerXForwardedFor)
                    .get()) {
            if (listResponse.getStatus() == UNAUTHORIZED.getStatusCode() || listResponse.getStatus() == FORBIDDEN.getStatusCode()) {
                LOG.log(WARNING, () -> "Authentication error while getting the series list, status: " + listResponse.getStatus());
                if (listResponse.getStatus() == UNAUTHORIZED.getStatusCode()) {
                    throw new NotAuthorizedException("Bearer", "Basic");
                } else {
                    throw new WebApplicationException(listResponse.getStatus());
                }
            } else if (listResponse.getStatusInfo().getFamily() != SUCCESSFUL) {
                LOG.log(SEVERE, () -> "Unable to successfully get the series list, status: " + listResponse.getStatus());
                throw new WebApplicationException(BAD_GATEWAY);
            }

            seriesList = listResponse.readEntity(new GenericType<List<Attributes>>() {});
        } catch (ProcessingException e) {
            LOG.log(SEVERE, "Error while accessing the studies list", e);
            throw new WebApplicationException(BAD_GATEWAY);
        }

        final WebTarget webTarget = CLIENT.target(wadoServiceURI)
                .path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
                .resolveTemplate("StudyInstanceUID", studyInstanceUID);

        final MultipartStreamingOutput multipartStreamingOutput = output -> {
            for (Attributes series : seriesList) {
                final String seriesInstanceUID = series.getString(Tag.SeriesInstanceUID);
                final AccessToken accessToken;
                try {
                    accessToken = accessTokenBuilder.withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID)).xForwardedFor(headerXForwardedFor).build();
                } catch (AccessTokenException e) {
                    LOG.log(WARNING, "Unable to get an access token", e);
                    throw new WebApplicationException(UNAUTHORIZED);
                }

                final Invocation.Builder invocationBuilder = webTarget.resolveTemplate("SeriesInstanceUID", seriesInstanceUID)
                        .request(requestAcceptType)
                        .header(AUTHORIZATION, accessToken.getHeaderValue());

                if (acceptCharsetParam != null) {
                    invocationBuilder.header(ACCEPT_CHARSET, acceptCharsetParam);
                }

                try (final Response wadoResponse = invocationBuilder.get(Response.class);
                     final InputStream inputStream = wadoResponse.readEntity(InputStream.class)
                ) {
                    if (!wadoResponse.getMediaType().isCompatible(acceptMediaType)) {
                        throw new ProcessingException("The response is not compatible");
                    }
                    final String boundary = wadoResponse.getMediaType().getParameters().get(BOUNDARY_PARAMETER);

                    final MultipartParser.Handler handler = (partNumber, multipartInputStream) -> {
                        Map<String, List<String>> sourceHeaders = multipartInputStream.readHeaderParams();
                        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
                        headers.putSingle(CONTENT_TYPE, sourceHeaders.get(CONTENT_TYPE).get(0));
                        // TODO handle content Location if needed

                        output.writePart(new StreamingBodyPart(multipartInputStream, MediaType.valueOf(headers.getFirst(CONTENT_TYPE)), headers));
                    };

                    new MultipartParser(boundary).parse(inputStream, handler);
                } catch (IOException | ProcessingException | WebApplicationException e) {
                    LOG.log(SEVERE, "Error while streaming the output", e);
                    throw new WebApplicationException(BAD_GATEWAY);
                }
            }
        };

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);

        return Response.ok(multipartStreamingOutput, requestAcceptType).cacheControl(cacheControl).build();
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
