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
import static javax.ws.rs.core.Response.Status.*;

@Path("/")
public final class WadoRSStudyInstance {
    private static final Logger LOG = Logger.getLogger(WadoRSStudyInstance.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient().register(JSONAttributesListMarshaller.class);

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";

    @Context
    ServletContext context;

    @HeaderParam(AUTHORIZATION)
    String authorizationHeader;

    @HeaderParam(HEADER_X_FORWARDED_FOR)
    String headerXForwardedFor;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("password/dicomweb/studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/instances")
    @Produces({"application/dicom+json;qs=1,multipart/related;type=\"application/dicom+xml\";qs=0.9,application/json;qs=0.8"})
    public Response wado(@PathParam("StudyInstanceUID") final String studyInstanceUID,
                         @QueryParam("offset") final Integer offsetParam,
                         @QueryParam("limit") final Integer limitParam,
                         @QueryParam("album") final String fromAlbumPk,
                         @QueryParam("inbox") final Boolean fromInbox) {

        final int offset = offsetParam != null ? max(offsetParam, 0) : 0;
        final int limit = limitParam != null ? limitParam : Integer.MAX_VALUE;

        final URI authorizationURI = getParameterURI("online.kheops.auth_server.uri");
        final URI instanceQidoServiceURI = getParameterURI("online.kheops.pacs.uri");

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

        final URI qidoServiceURI = qidoServiceURIBuilder.build(studyInstanceUID);

        final List<Attributes> seriesList;
        try {
            seriesList = CLIENT.target(qidoServiceURI).request(MediaTypes.APPLICATION_DICOM_JSON_TYPE)
                    .header(AUTHORIZATION, authorizationHeader)
                    .header(HEADER_X_FORWARDED_FOR, headerXForwardedFor)
                    .get(new GenericType<List<Attributes>>() {});

        } catch (ProcessingException e) {
            LOG.log(SEVERE, "Error getting the series list", e);
            throw new WebApplicationException(BAD_GATEWAY);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == UNAUTHORIZED.getStatusCode()) {
                LOG.log(WARNING, "User Unauthorized", e);
                throw new WebApplicationException(UNAUTHORIZED);
            } else if (e.getResponse().getStatus() == NOT_FOUND.getStatusCode()) {
                LOG.log(WARNING, "Study Not Found", e);
                throw new WebApplicationException(NOT_FOUND);
            } else {
                LOG.log(WARNING, "Unexpected response", e);
                throw new WebApplicationException(BAD_GATEWAY);
            }
        }
        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        UriBuilder instanceQidoServiceURIBuilder = UriBuilder.fromUri(instanceQidoServiceURI);

        for (Map.Entry<String, List<String>> parameter: queryParameters.entrySet()) {
            if (!parameter.getKey().equalsIgnoreCase("offset") &&
                    !parameter.getKey().equalsIgnoreCase("limit") &&
                    !parameter.getKey().equalsIgnoreCase("album") &&
                    !parameter.getKey().equalsIgnoreCase("inbox")) {
                instanceQidoServiceURIBuilder = instanceQidoServiceURIBuilder.queryParam(parameter.getKey(), parameter.getValue().toArray());
            } else if (parameter.getKey().equalsIgnoreCase("limit")) {
                instanceQidoServiceURIBuilder = instanceQidoServiceURIBuilder.queryParam(parameter.getKey(), limit+1);
            }
        }

        final WebTarget webTarget = CLIENT.target(instanceQidoServiceURIBuilder.build())
                .path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances")
                .resolveTemplate("StudyInstanceUID", studyInstanceUID);

        int instancesToSkip = offset;
        boolean additionalInstances = false;
        final List<Attributes> instanceList = new ArrayList<>();
        int i;
        for (i = 0; i < seriesList.size() && instanceList.size() < limit; i++) {
            String seriesInstanceUID = seriesList.get(i).getString(Tag.SeriesInstanceUID);
            final WebTarget instanceWebTarget = webTarget.resolveTemplate("SeriesInstanceUID", seriesInstanceUID);

            final AccessToken accessToken;
            try {
                accessToken = accessTokenBuilder.withSeriesID(new SeriesID(studyInstanceUID, seriesInstanceUID)).xForwardedFor(headerXForwardedFor).build();
            } catch (AccessTokenException e) {
                LOG.log(SEVERE, "Unable to get an access token", e);
                throw new NotAuthorizedException("Bearer", "Basic");
            }

            final List<Attributes> seriesInstanceList;
            try {
                seriesInstanceList = instanceWebTarget.request(MediaTypes.APPLICATION_DICOM_JSON_TYPE)
                        .header(AUTHORIZATION, accessToken.getHeaderValue())
                        .get(new GenericType<List<Attributes>>() {});
            } catch (ProcessingException | WebApplicationException e) {
                LOG.log(SEVERE, "Unable to get instances for a series", e);
                throw new WebApplicationException(BAD_GATEWAY);
            }

            final int localInstancesToSkip = min(instancesToSkip, seriesInstanceList.size());
            if (localInstancesToSkip < seriesInstanceList.size()) {
                int instancesToAdd = min(seriesInstanceList.size() - localInstancesToSkip, limit - instanceList.size());
                if (instancesToAdd < seriesInstanceList.size() - localInstancesToSkip) {
                    additionalInstances = true;
                }
                instanceList.addAll(seriesInstanceList.subList(localInstancesToSkip, localInstancesToSkip + instancesToAdd));
            }
            instancesToSkip -= localInstancesToSkip;
        }
        if (i < seriesList.size()) {
            additionalInstances = true;
        }

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);

        if (instanceList.isEmpty()) {
            return Response.noContent().cacheControl(cacheControl).build();
        } else {
            final GenericEntity<List<Attributes>> genericInstanceList = new GenericEntity<List<Attributes>>(instanceList) {};
            if (additionalInstances) {
                return Response.ok(genericInstanceList).header("Warning", "299 {+service}: There are unknown additional results that can be requested")
                        .cacheControl(cacheControl).build();
            } else {
                return Response.ok(genericInstanceList).cacheControl(cacheControl).build();
            }
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

