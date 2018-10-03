package online.kheops.auth_server.resource;


import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.PACSAuthTokenBuilder;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.KheopsPrincipalInterface;
import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.marshaller.JSONAttributesListMarshaller;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.Consts;
import online.kheops.auth_server.util.PairListXTotalCount;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Context;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.series.Series.checkValidUID;
import static online.kheops.auth_server.sharing.Sending.availableSeriesUIDs;
import static online.kheops.auth_server.study.Studies.findAttributesByUserPKJOOQ;
import static online.kheops.auth_server.util.JOOQTools.getDataSource;

@Path("/")
public class QIDOResource {

    private static final Logger LOG = Logger.getLogger(QIDOResource.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient().register(JSONAttributesListMarshaller.class);

    @Context
    private UriInfo uriInfo;

    @Context
    ServletContext context;

    @GET
    @Secured
    @Path("studies")
    @Produces({"application/dicom+json;qs=1,multipart/related;type=\"application/dicom+xml\";qs=0.9,application/json;qs=0.8"})
    public Response getStudies(@QueryParam("album") Long fromAlbumPk,
                               @QueryParam("inbox") Boolean fromInbox,
                               @QueryParam("offset") Integer offset,
                               @Context SecurityContext securityContext) {

        if (fromAlbumPk != null && fromInbox != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        if (offset == null) {
            offset = 0;
        }

        final long callingUserPk = ((KheopsPrincipalInterface)securityContext.getUserPrincipal()).getDBID();


        final PairListXTotalCount<Attributes> pair;

        try (Connection connection = getDataSource().getConnection()) {
            pair = findAttributesByUserPKJOOQ(callingUserPk, uriInfo.getQueryParameters(), connection);
            LOG.info("QueryParameters : " + uriInfo.getQueryParameters().toString());
        } catch (BadRequestException e) {
            LOG.log(Level.SEVERE, "Error 400 :", e);
            return Response.status(Response.Status.BAD_REQUEST).entity("The QIDO-RS Provider was unable to perform the query because the Service Provider cannot understand the query component. [" + e.getMessage() + "]").build();
        } catch (BadQueryParametersException e) {
            LOG.log(Level.INFO, e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while connecting to the database", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database Connection Error").build();
        }
        if(pair.getXTotalCount() == 0) {
            return Response.status(Response.Status.NO_CONTENT).header("X-Total-Count", pair.getXTotalCount()).build();
        }

        GenericEntity<List<Attributes>> genericAttributesList = new GenericEntity<List<Attributes>>(pair.getAttributesList()) {};
        Response.ResponseBuilder response = Response.ok(genericAttributesList).header("X-Total-Count", pair.getXTotalCount());

        final long remaining = pair.getXTotalCount() - (offset + pair.getAttributesList().size());
        if ( remaining > 0) {
            // TODO fix {+service}
            response.header("Warning","Warning: 299 {+service}: There are "+ remaining +" additional results that can be requested");
        }

        return response.build();
    }

    @GET
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series")
    @Produces({"application/dicom+json;qs=1,multipart/related;type=\"application/dicom+xml\";qs=0.9,application/json;qs=0.8"})
    public Response getSeries(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                              @QueryParam("album") Long fromAlbumPk,
                              @QueryParam("inbox") Boolean fromInbox,
                              @QueryParam("offset") Integer offset,
                              @QueryParam("limit") Integer limit,
                              @Context SecurityContext securityContext,
                              @Context UriInfo uriInfo) {

        if ((fromAlbumPk != null && fromInbox != null)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        fromInbox = fromInbox != null;
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = Integer.MAX_VALUE;
        }

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        URI uri = UriBuilder.fromUri(getDicomWebURI()).path("studies/{StudyInstanceUID}/series").build(studyInstanceUID);
        String authToken = PACSAuthTokenBuilder.newBuilder().withStudyUID(studyInstanceUID).withAllSeries().build();

        WebTarget webTarget = CLIENT.target(uri);

        for (String parameter: queryParameters.keySet()) {
            webTarget = webTarget.queryParam(parameter, queryParameters.get(parameter).toArray());
        }

        final Set<String> availableSeriesUIDs;
        try {
            availableSeriesUIDs = availableSeriesUIDs(callingUserPk, studyInstanceUID, fromAlbumPk, fromInbox);
        } catch (UserNotFoundException | AlbumNotFoundException | StudyNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        if (availableSeriesUIDs.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).header("X-Total-Count", 0).build();
        }

        List<Attributes> allSeries = webTarget.request("application/dicom+json")
                .header("Authorization", "Bearer "+authToken)
                .get(new GenericType<List<Attributes>>() {});

        List<Attributes> availableSeries = new ArrayList<>();

        int skipped = 0;
        int totalAvailableSeries = 0;
        for (Attributes series: allSeries) {
            String seriesInstanceUID = series.getString(Tag.SeriesInstanceUID);
            if (availableSeriesUIDs.contains(seriesInstanceUID)) {
                totalAvailableSeries++;
                if (skipped >= offset) {
                    if (availableSeries.size() < limit) {
                        availableSeries.add(series);
                    }
                } else {
                    skipped++;
                }
            }
        }
        GenericEntity<List<Attributes>> genericAvailableSeries = new GenericEntity<List<Attributes>>(availableSeries) {};

        Response.ResponseBuilder responseBuilder = Response.ok().entity(genericAvailableSeries).header("X-Total-Count", totalAvailableSeries);

        if (totalAvailableSeries > availableSeries.size() + offset) {
            int remaining = totalAvailableSeries - (offset + availableSeries.size());
            // TODO fix {+service}
            responseBuilder.header("Warning", "299 {+service}: There are " + remaining + " additional results that can be requested");
        }

        return responseBuilder.build();
    }

    @GET
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/metadata")
    @Produces("application/dicom+json;qs=1,application/json;qs=0.9")
    public Response getStudiesMetadata(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                       @QueryParam("album") Long fromAlbumPk,
                                       @QueryParam("inbox") Boolean fromInbox, @Context SecurityContext securityContext) {

        if ((fromAlbumPk != null && fromInbox != null)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        fromInbox = fromInbox != null;

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        URI uri = UriBuilder.fromUri(getDicomWebURI()).path("studies/{StudyInstanceUID}/metadata").build(studyInstanceUID);
        String authToken = PACSAuthTokenBuilder.newBuilder().withStudyUID(studyInstanceUID).withAllSeries().build();
        InputStream is = CLIENT.target(uri).request("application/dicom+json").header("Authorization", "Bearer "+authToken).get(InputStream.class);

        JsonParser parser = Json.createParser(is);
        JSONReader jsonReader = new JSONReader(parser);

        final Set<String> availableSeriesUIDs;
        try {
            availableSeriesUIDs = availableSeriesUIDs(callingUserPk, studyInstanceUID, fromAlbumPk, fromInbox);
        } catch (UserNotFoundException | AlbumNotFoundException | StudyNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        final StreamingOutput stream = os -> {
            final JsonGenerator generator = Json.createGenerator(os);
            final JSONWriter jsonWriter = new JSONWriter(generator);

            generator.writeStartArray();

            try {
                jsonReader.readDatasets((fmi, dataset) -> {
                    if (availableSeriesUIDs.contains(dataset.getString(Tag.SeriesInstanceUID))) {
                        jsonWriter.write(dataset);
                    }
                });
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error while processing metadata", e);
                throw new WebApplicationException(e);
            }

            generator.writeEnd();
            generator.flush();
        };

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
