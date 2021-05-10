package online.kheops.auth_server.resource;


import online.kheops.auth_server.EntityManagerListener;
import online.kheops.auth_server.NotAlbumScopeTypeException;
import online.kheops.auth_server.PepAccessTokenBuilder;
import online.kheops.auth_server.album.AlbumForbiddenException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.annotation.*;
import online.kheops.auth_server.marshaller.JSONAttributesListMarshaller;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.util.*;
import online.kheops.auth_server.util.KheopsLogBuilder.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.Response.Status.*;
import static online.kheops.auth_server.filter.AlbumPermissionSecuredContext.QUERY_PARAM;
import static online.kheops.auth_server.filter.SecuredFilter.LINK_AUTH;
import static online.kheops.auth_server.sharing.Sending.availableSeriesUIDs;
import static online.kheops.auth_server.study.Studies.findAttributesByUserPKJOOQ;
import static online.kheops.auth_server.user.AlbumUserPermissions.READ_SERIES;
import static online.kheops.auth_server.util.Consts.*;
import static online.kheops.auth_server.util.Consts.UserInRole.VIEWER_TOKEN;
import static online.kheops.auth_server.util.ErrorResponse.Message.*;
import static online.kheops.auth_server.util.HttpHeaders.X_TOTAL_COUNT;

@Path("/")
public class QIDOResource {

    private static final Logger LOG = Logger.getLogger(QIDOResource.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient().register(JSONAttributesListMarshaller.class);

    @Context
    private UriInfo uriInfo;

    @Context
    private ServletContext context;

    @Context
    private SecurityContext securityContext;

    @GET
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = READ_SERIES, context = QUERY_PARAM)
    @Path("studies")
    @Produces({"application/dicom+json;qs=1,multipart/related;type=\"application/dicom+xml\";qs=0.9,application/json;qs=0.8"})
    public Response getStudies(@QueryParam(ALBUM) String fromAlbumId,
                               @QueryParam(INBOX) Boolean fromInbox)
            throws AlbumNotFoundException, AlbumForbiddenException, BadQueryParametersException {

        if (fromAlbumId != null && fromInbox != null) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Use only '"+ALBUM+"' xor '"+INBOX+"' not both")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());
        final long callingUserPk = kheopsPrincipal.getUser().getPk();

        if(fromInbox != null && fromInbox && !kheopsPrincipal.hasUserAccess()) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(AUTHORIZATION_ERROR)
                    .detail("This authorization is not available for access to the inbox")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        final PairListXTotalCount<Attributes> pair;
        final StudyQIDOParams qidoParams;
        try (Connection connection = EntityManagerListener.getConnection()) {
            qidoParams = new StudyQIDOParams(kheopsPrincipal, uriInfo.getQueryParameters());
            pair = findAttributesByUserPKJOOQ(callingUserPk, qidoParams, connection);
        } catch (BadRequestException e) {
            LOG.log(Level.SEVERE, "Error 400 :", e);
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("error")
                    .detail("The QIDO-RS Provider was unable to perform the query because the Service Provider cannot understand the query component.")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        } catch (NoResultException e) {
            return Response.status(NO_CONTENT).header(X_TOTAL_COUNT, 0).build();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error while connecting to the database", e);
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("Database error")
                    .detail("Database Connection Error")
                    .build();
            return Response.status(INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
        if(pair.getAttributesList().isEmpty()) {
            return Response.status(NO_CONTENT)
                    .header(X_TOTAL_COUNT, pair.getXTotalCount())
                    .build();
        }

        GenericEntity<List<Attributes>> genericAttributesList = new GenericEntity<List<Attributes>>(pair.getAttributesList()) {};
        Response.ResponseBuilder response = Response.ok(genericAttributesList)
                .header(X_TOTAL_COUNT, pair.getXTotalCount());

        final long remaining = pair.getXTotalCount() - (qidoParams.getOffset().orElse(0) + pair.getAttributesList().size());
        if ( remaining > 0) {
            // TODO fix {+service}
            response.header("Warning","Warning: 299 {+service}: There are "+ remaining +" additional results that can be requested");
        }

        final CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setNoCache(true);
        response.cacheControl(cacheControl);

        KheopsLogBuilder kheopsLogBuilder = kheopsPrincipal.getKheopsLogBuilder().action(ActionType.QIDO_STUDIES);
        if (fromAlbumId != null) {
            kheopsLogBuilder.album(fromAlbumId);
        } else if (fromInbox != null && fromInbox) {
            kheopsLogBuilder.album("inbox");
        } else {
            kheopsLogBuilder.album("inbox/all_albums");
        }
        kheopsLogBuilder.log();
        return response.build();
    }

    @GET
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = READ_SERIES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series")
    @Produces({"application/dicom+json;qs=1,multipart/related;type=\"application/dicom+xml\";qs=0.9,application/json;qs=0.8"})
    public Response getSeries(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                              @QueryParam(ALBUM) String fromAlbumId,
                              @QueryParam(INBOX) Boolean fromInbox,
                              @QueryParam(FAVORITE) Boolean favoriteFilter,
                              @QueryParam(QUERY_PARAMETER_OFFSET) Integer offset,
                              @QueryParam(QUERY_PARAMETER_LIMIT) Integer limit)
            throws AlbumNotFoundException, StudyNotFoundException, BadQueryParametersException{

        if (fromAlbumId != null && fromInbox != null) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Use only '"+ALBUM+"' or '"+INBOX+"' not both")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        final boolean includeFieldFavorite;
        includeFieldFavorite = uriInfo.getQueryParameters().containsKey(INCLUDE_FIELD) && (uriInfo.getQueryParameters().get(INCLUDE_FIELD).contains(String.format("%08X", CUSTOM_DICOM_TAG_FAVORITE)) ||
                uriInfo.getQueryParameters().get(INCLUDE_FIELD).contains(FAVORITE));

        if(fromAlbumId == null && fromInbox == null) {
            if(includeFieldFavorite) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("when including field favorite(0x0001,2345), you must specify "+INBOX+"=true OR "+ALBUM+"={album_id} as a query param")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
            if(favoriteFilter != null) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(BAD_QUERY_PARAMETER)
                        .detail("If favorite is set, you must specify "+INBOX+"=true OR "+ALBUM+"={album_ID} as query param")
                        .build();
                return Response.status(BAD_REQUEST).entity(errorResponse).build();
            }
        }

        final Comparator<Attributes> sortComparator = SeriesQIDOSortParams.sortComparator(uriInfo.getQueryParameters());

        fromInbox = fromInbox != null;

        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = Integer.MAX_VALUE;
        }

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        KheopsLogBuilder kheopsLogBuilder = kheopsPrincipal.getKheopsLogBuilder().action(ActionType.QIDO_SERIES)
                .study(studyInstanceUID);

        if (!kheopsPrincipal.hasStudyViewAccess(studyInstanceUID)) {
            return Response.status(NOT_FOUND).build();
        }

        //BEGIN kheopsPrincipal
        if (Boolean.TRUE.equals(fromInbox) && !kheopsPrincipal.hasInboxAccess()) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(AUTHORIZATION_ERROR)
                    .detail("This authorization is not available for access to the inbox")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        try {
            if (fromAlbumId != null && !fromAlbumId.equals(kheopsPrincipal.getAlbumID())) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(AUTHORIZATION_ERROR)
                        .detail("This authorization is not available for access to this album")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            } else if (fromAlbumId == null) {
                fromAlbumId = kheopsPrincipal.getAlbumID();
            }
        } catch (NotAlbumScopeTypeException e) { /*empty*/ }
        catch (AlbumNotFoundException e) {
              return Response.status(FORBIDDEN).entity(e.getErrorResponse()).build();
        }

        if(securityContext.isUserInRole(VIEWER_TOKEN)) {
            fromInbox = kheopsPrincipal.hasInboxAccess();
        }
        //END kheopsPrincipal

        final MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.putAll(uriInfo.getQueryParameters());
        queryParameters.remove(ALBUM);
        queryParameters.remove(INBOX);
        queryParameters.remove(QUERY_PARAMETER_OFFSET);
        queryParameters.remove(QUERY_PARAMETER_LIMIT);
        queryParameters.remove(FAVORITE);
        queryParameters.remove(QUERY_PARAMETER_SORT);

        URI uri = UriBuilder.fromUri(getDicomWebURI()).path("studies/{StudyInstanceUID}/series").build(studyInstanceUID);
        String authToken = PepAccessTokenBuilder.newBuilder(kheopsPrincipal)
                .withStudyUID(studyInstanceUID)
                .withAllSeries()
                .withSubject(kheopsPrincipal.getUser().getSub())
                .build();

        WebTarget webTarget = CLIENT.target(uri);

        for (Map.Entry<String, List<String>> queryParameterEntry: queryParameters.entrySet()) {
            webTarget = webTarget.queryParam(queryParameterEntry.getKey(), queryParameterEntry.getValue().toArray());
        }

        final Map<String, Boolean> availableSeriesUIDs;
        availableSeriesUIDs = availableSeriesUIDs(kheopsPrincipal.getUser(), studyInstanceUID, fromAlbumId, fromInbox);

        if (availableSeriesUIDs.isEmpty()) {
            return Response.status(NO_CONTENT)
                    .header(X_TOTAL_COUNT, 0)
                    .build();
        }

        final List<Attributes> allSeries;
        try {
            allSeries = webTarget.request("application/dicom+json")
                    .header("Authorization", "Bearer " + authToken)
                    .get(new GenericType<List<Attributes>>() {});
        } catch (WebApplicationException | ProcessingException e) {
            LOG.log(Level.SEVERE, "Error getting the list of series from the DCM server", e);
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message("PACS error")
                    .detail("Error getting the list of series from the DCM server")
                    .build();
            return Response.status(BAD_GATEWAY).entity(errorResponse).build();
        }

        List<Attributes> availableSeries = new ArrayList<>();

        int skipped = 0;
        int totalAvailableSeries = 0;
        boolean favoriteValue = false;
        for (Attributes series: allSeries) {
            String seriesInstanceUID = series.getString(Tag.SeriesInstanceUID);
            if (availableSeriesUIDs.containsKey(seriesInstanceUID)) {
                totalAvailableSeries++;
                if(favoriteFilter != null || includeFieldFavorite) {
                    favoriteValue = availableSeriesUIDs.get(seriesInstanceUID);
                }

                if (skipped >= offset) {
                    if(!(favoriteFilter != null && favoriteValue != favoriteFilter) && availableSeries.size() < limit) {
                        if (includeFieldFavorite) {
                            series.setString(CUSTOM_DICOM_TAG_FAVORITE, VR.SH, String.valueOf(favoriteValue));
                        }
                        final StringBuilder retrieveURL = new StringBuilder();
                        retrieveURL.append(context.getInitParameter(HOST_ROOT_PARAMETER));
                        retrieveURL.append("/api");
                        if (securityContext.getAuthenticationScheme().equals(LINK_AUTH)) {
                            retrieveURL.append("/link/").append(kheopsPrincipal.getOriginalToken());
                        }
                        retrieveURL.append("/studies/");
                        retrieveURL.append(studyInstanceUID);
                        retrieveURL.append("/series/");
                        retrieveURL.append(series.getString(Tag.SeriesInstanceUID));
                        series.setString(Tag.RetrieveURL, VR.UR, retrieveURL.toString());
                        availableSeries.add(series);
                        kheopsLogBuilder.series(seriesInstanceUID);
                    }
                } else {
                    skipped++;
                }
            }
        }

        availableSeries.sort(sortComparator);

        GenericEntity<List<Attributes>> genericAvailableSeries = new GenericEntity<List<Attributes>>(availableSeries) {};

        Response.ResponseBuilder responseBuilder = Response.ok(genericAvailableSeries)
                .header(X_TOTAL_COUNT, totalAvailableSeries);

        if (totalAvailableSeries > availableSeries.size() + offset) {
            int remaining = totalAvailableSeries - (offset + availableSeries.size());
            // TODO fix {+service}
            responseBuilder.header("Warning", "299 {+service}: There are " + remaining + " additional results that can be requested");
        }

        if (fromAlbumId != null) {
            kheopsLogBuilder.album(fromAlbumId);
        } else if (Boolean.TRUE.equals(fromInbox)) {
            kheopsLogBuilder.album("inbox");
        }
        kheopsLogBuilder.log();
        return responseBuilder.build();
    }

    @GET
    @Secured
    @AlbumAccessSecured
    @AlbumPermissionSecured(permission = READ_SERIES, context = QUERY_PARAM)
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/metadata")
    @Produces("application/dicom+json;qs=1,application/json;qs=0.9")
    public Response getStudiesMetadata(@PathParam(STUDY_INSTANCE_UID) @UIDValidator String studyInstanceUID,
                                       @QueryParam(ALBUM) String fromAlbumId,
                                       @QueryParam(INBOX) Boolean fromInbox)
            throws AlbumNotFoundException, StudyNotFoundException {

        if ((fromAlbumId != null && fromInbox != null)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("Use only '"+ALBUM+"' xor '"+INBOX+"' not both")
                    .build();
            return Response.status(BAD_REQUEST).entity(errorResponse).build();
        }

        fromInbox = fromInbox != null;

        final KheopsPrincipal kheopsPrincipal = ((KheopsPrincipal)securityContext.getUserPrincipal());

        KheopsLogBuilder kheopsLogBuilder = kheopsPrincipal.getKheopsLogBuilder().action(ActionType.QIDO_STUDY_METADATA)
                .study(studyInstanceUID);

        if (!kheopsPrincipal.hasStudyViewAccess(studyInstanceUID)) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(STUDY_NOT_FOUND)
                    .detail("The study does not exist or you don't have access")
                    .build();
            return Response.status(NOT_FOUND).entity(errorResponse).build();
        }

        //BEGIN kheopsPrincipal
        if (Boolean.TRUE.equals(fromInbox) && !kheopsPrincipal.hasInboxAccess()) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(AUTHORIZATION_ERROR)
                    .detail("This authorization is not available for access to the inbox")
                    .build();
            return Response.status(FORBIDDEN).entity(errorResponse).build();
        }

        try {
            if (fromAlbumId != null && !fromAlbumId.equals(kheopsPrincipal.getAlbumID())) {
                final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                        .message(AUTHORIZATION_ERROR)
                        .detail("This authorization is not available for access to this album")
                        .build();
                return Response.status(FORBIDDEN).entity(errorResponse).build();
            } else if (fromAlbumId == null) {
                fromAlbumId = kheopsPrincipal.getAlbumID();
            }
        } catch (NotAlbumScopeTypeException e) { /*empty*/ }
        catch (AlbumNotFoundException e) {
            return Response.status(FORBIDDEN).entity(e.getErrorResponse()).build();
        }

        if(securityContext.isUserInRole(VIEWER_TOKEN)) {
            fromInbox = kheopsPrincipal.hasInboxAccess();
        }
        //END kheopsPrincipal

        URI uri = UriBuilder.fromUri(getDicomWebURI()).path("studies/{StudyInstanceUID}/metadata").build(studyInstanceUID);
        String authToken = PepAccessTokenBuilder.newBuilder(kheopsPrincipal)
                .withStudyUID(studyInstanceUID)
                .withAllSeries()
                .withSubject(kheopsPrincipal.getUser().getSub())
                .build();
        final Response upstreamResponse;
        try {
            upstreamResponse = CLIENT.target(uri).request("application/dicom+json").header("Authorization", "Bearer " + authToken).get();
        } catch (ProcessingException e) {
            LOG.log(WARNING, "unable to reach upstream", e);
            return Response.status(BAD_GATEWAY).build();
        }
        if (upstreamResponse.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            LOG.log(WARNING, () -> "bad response from upstream status = " + upstreamResponse.getStatus() + "\n" + upstreamResponse);
            return Response.status(BAD_GATEWAY).build();
        }

        final Map<String, Boolean> availableSeriesUIDs;
        availableSeriesUIDs = availableSeriesUIDs(kheopsPrincipal.getUser(), studyInstanceUID, fromAlbumId, fromInbox);

        final StreamingOutput stream = os -> {
            try (final InputStream inputStream = upstreamResponse.readEntity(InputStream.class);
                 final JsonParser parser = Json.createParser(inputStream);
                 final JsonGenerator generator = Json.createGenerator(os)) {

                final JSONReader jsonReader = new JSONReader(parser);
                final JSONWriter jsonWriter = new JSONWriter(generator);

                generator.writeStartArray();

                jsonReader.readDatasets((fmi, dataset) -> {
                    if (availableSeriesUIDs.containsKey(dataset.getString(Tag.SeriesInstanceUID))) {
                        jsonWriter.write(dataset);
                        kheopsLogBuilder.series(dataset.getString(Tag.SeriesInstanceUID));
                    }
                });
                generator.writeEnd();
                generator.flush();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error while processing metadata", e);
                throw new WebApplicationException("Error while processing metadata", e);
            } finally {
                upstreamResponse.close();
            }
        };

        if (fromAlbumId != null) {
            kheopsLogBuilder.album(fromAlbumId);
        } else if (Boolean.TRUE.equals(fromInbox)) {
            kheopsLogBuilder.album("inbox");
        }
        kheopsLogBuilder.log();
        return Response.ok(stream).build();
    }

    private URI getDicomWebURI() {
        try {
            return new URI(context.getInitParameter("online.kheops.pacs.uri"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("online.kheops.pacs.uri is not a valid URI", e);
        }
    }
}
