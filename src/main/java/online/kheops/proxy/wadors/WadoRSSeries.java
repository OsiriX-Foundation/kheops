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

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.glassfish.jersey.media.multipart.Boundary.BOUNDARY_PARAMETER;

@Path("/")
public class WadoRSSeries {
    private static final Logger LOG = Logger.getLogger(WadoRSSeries.class.getName());
    private static final Client CLIENT = newClient();

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


    @GET
    @Path("/studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/")
    @Produces("multipart/related")
    public Response wado(@PathParam("StudyInstanceUID") String studyInstanceUID,
                         @QueryParam("album") Long fromAlbumPk,
                         @QueryParam("inbox") Boolean fromInbox) {
        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        final URI wadoServiceURI = getParameterURI("online.kheops.pacs.uri");

        final AuthorizationToken authorizationToken = AuthorizationToken.fromAuthorizationHeader(authorizationHeader);
        final AccessToken.AccessTokenBuilder accessTokenBuilder =  AccessToken.createBuilder(authorizationURI)
                .withCapability(authorizationToken.getToken());

        final UriBuilder qidoServiceURIBuilder = UriBuilder.fromUri(authorizationURI)
                .path("/studies/{StudyInstanceUID}/series");

        if (fromAlbumPk != null) {
            qidoServiceURIBuilder.queryParam("album", fromAlbumPk);
        }
        if (fromInbox != null) {
            qidoServiceURIBuilder.queryParam("inbox", fromInbox);
        }

        final URI qidoServiceURI = qidoServiceURIBuilder.build(studyInstanceUID);

        final List<Attributes> seriesList = CLIENT.target(qidoServiceURI).request(MediaTypes.APPLICATION_DICOM_JSON_TYPE)
                .header(AUTHORIZATION, authorizationHeader)
                .get(new GenericType<List<Attributes>>() {
                });

        final WebTarget webTarget = CLIENT.target(wadoServiceURI)
                .path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}")
                .resolveTemplate("StudyInstanceUID", studyInstanceUID);

        final MultipartStreamingOutput multipartStreamingOutput = output -> {
            for (Attributes series : seriesList) {
                final String seriesInstanceUID = series.getString(Tag.SeriesInstanceUID);
                final WebTarget wadoWebTarget = webTarget.resolveTemplate("SeriesInstanceUID", seriesInstanceUID);

                final AccessToken accessToken;
                try {
                    accessToken = accessTokenBuilder.withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID)).build();
                } catch (AccessTokenException e) {
                    LOG.log(WARNING, "Unable to get an access token", e);
                    throw new WebApplicationException(UNAUTHORIZED);
                }

                final Invocation.Builder invocationBuilder = wadoWebTarget.request();
                invocationBuilder.header(AUTHORIZATION, "Bearer " + accessToken);
                if (acceptParam != null) {
                    invocationBuilder.header(ACCEPT, acceptParam);
                }
                if (acceptCharsetParam != null) {
                    invocationBuilder.header(ACCEPT_CHARSET, acceptCharsetParam);
                }

                final Response wadoResponse = invocationBuilder.get(Response.class);
                final InputStream inputStream = wadoResponse.readEntity(InputStream.class);
                final String boundary = wadoResponse.getMediaType().getParameters().get(BOUNDARY_PARAMETER);

                final MultipartParser.Handler handler = (partNumber, multipartInputStream) -> {
                    Map<String, List<String>> sourceHeaders = multipartInputStream.readHeaderParams();
                    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
                    headers.putSingle(CONTENT_TYPE, sourceHeaders.get(CONTENT_TYPE).get(0));
                    // TODO handle content Location if needed

                    output.writePart(new StreamingBodyPart(multipartInputStream, MediaType.valueOf(headers.getFirst(CONTENT_TYPE)), headers));
                };

                new MultipartParser(boundary).parse(inputStream, handler);
                wadoResponse.close();
            }
        };

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);

        return Response.ok(multipartStreamingOutput).cacheControl(cacheControl).build();
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
