package online.kheops.proxy.wadors;

import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.marshaller.JSONAttributesListMarshaller;
import online.kheops.proxy.tokens.AccessToken;
import online.kheops.proxy.tokens.AccessTokenException;
import online.kheops.proxy.tokens.AuthorizationToken;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.ws.rs.MediaTypes;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path("/")
public class WadoRSStudyInstance {
    private static final Logger LOG = Logger.getLogger(WadoRSSeries.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient().register(JSONAttributesListMarshaller.class);

    @Context
    ServletContext context;

    @HeaderParam(AUTHORIZATION)
    String authorizationHeader;

    @Context
    UriInfo uriInfo;


    @GET
    @Path("password/dicomweb/studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/instances")
    @Produces({"application/dicom+json;qs=1,multipart/related;type=\"application/dicom+xml\";qs=0.9,application/json;qs=0.8"})
    public Response wado(@PathParam("StudyInstanceUID") String studyInstanceUID,
                         @QueryParam("offset") Integer offsetParam,
                         @QueryParam("limit") Integer limitParam,
                         @QueryParam("album") Long fromAlbumPk,
                         @QueryParam("inbox") Boolean fromInbox) {

        final int offset = offsetParam != null ? max(offsetParam, 0) : 0;
        final int limit = limitParam != null ? limitParam : Integer.MAX_VALUE;

        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        final URI instanceQidoServiceURI = getParameterURI("online.kheops.pacs.uri");

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
                .get(new GenericType<List<Attributes>>() {});

        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        UriBuilder instanceQidoServiceURIBuilder = UriBuilder.fromUri(instanceQidoServiceURI);

        for (Map.Entry<String, List<String>> parameter: queryParameters.entrySet()) {
            instanceQidoServiceURIBuilder = instanceQidoServiceURIBuilder.queryParam(parameter.getKey(), parameter.getValue().toArray());
        }

        final WebTarget webTarget = CLIENT.target(instanceQidoServiceURIBuilder.build())
                .path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances")
                .resolveTemplate("StudyInstanceUID", studyInstanceUID);

        int instancesToSkip = offset;
        final List<Attributes> instanceList = new ArrayList<>();
        for (int i = 0; i < seriesList.size() && instanceList.size() < limit; i++) {
            String seriesInstanceUID = seriesList.get(i).getString(Tag.SeriesInstanceUID);
            final WebTarget instanceWebTarget = webTarget.resolveTemplate("SeriesInstanceUID", seriesInstanceUID);

            final AccessToken accessToken;
            try {
                accessToken = accessTokenBuilder.withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID)).build();
            } catch (AccessTokenException e) {
                LOG.log(WARNING, "Unable to get an access token", e);
                throw new WebApplicationException(UNAUTHORIZED);
            }

            final List<Attributes> seriesInstanceList = instanceWebTarget.request(MediaTypes.APPLICATION_DICOM_JSON_TYPE)
                    .header(AUTHORIZATION, accessToken.getHeaderValue())
                    .get(new GenericType<List<Attributes>>() {});

            final int localInstancesToSkip = min(instancesToSkip, seriesInstanceList.size());
            if (localInstancesToSkip < seriesInstanceList.size()) {
                int instancesToAdd = min(seriesInstanceList.size() - localInstancesToSkip, limit - instanceList.size());
                instanceList.addAll(seriesInstanceList.subList(localInstancesToSkip, localInstancesToSkip + instancesToAdd));
            }
            instancesToSkip -= localInstancesToSkip;
        }

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);

        if (instanceList.isEmpty()) {
            return Response.noContent().cacheControl(cacheControl).build();
        } else {
            final GenericEntity<List<Attributes>> genericInstanceList = new GenericEntity<List<Attributes>>(instanceList) {};
            return Response.ok(genericInstanceList).cacheControl(cacheControl).build();
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

