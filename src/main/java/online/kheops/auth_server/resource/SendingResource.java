package online.kheops.auth_server.resource;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;

import online.kheops.auth_server.KheopsPrincipal;
import online.kheops.auth_server.PACSAuthTokenBuilder;
import online.kheops.auth_server.album.AlbumForbiddenException;
import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.annotation.Secured;
import online.kheops.auth_server.capability.CapabilitiesResponses;
import online.kheops.auth_server.marshaller.JSONAttributesListMarshaller;
import online.kheops.auth_server.series.SeriesForbiddenException;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.sharing.Sending;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.Consts;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static online.kheops.auth_server.series.Series.checkValidUID;
import static online.kheops.auth_server.sharing.Sending.availableSeriesUIDs;


@Path("/")
public class SendingResource
{

    private static final Logger LOG = Logger.getLogger(SendingResource.class.getName());

    @Context
    ServletContext context;

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareStudyWithUser(@PathParam("user") String username,
                                       @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                       @QueryParam("album") Long fromAlbumPk,
                                       @QueryParam("inbox") Boolean fromInbox,
                                       @Context SecurityContext securityContext) {

        if ((fromAlbumPk != null && fromInbox != null)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        if (fromInbox != null) {
            fromInbox = true;
        } else {
            fromInbox = false;
        }

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.shareStudyWithUser(callingUserPk, username, studyInstanceUID, fromAlbumPk, fromInbox);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" with "+username);
        return Response.status(Response.Status.CREATED).build();
    }


    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/users/{user}")
    public Response shareSeriesWithUser(@PathParam("user") String username,
                                        @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                        @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                        @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.shareSeriesWithUser(callingUserPk, username, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" with "+username);
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    public Response putSeries(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                              @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                              @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.appropriateSeries(callingUserPk, studyInstanceUID, seriesInstanceUID);
        } catch(SeriesForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (UserNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }

        return Response.status(Response.Status.CREATED).build();
    }

    private static final Client CLIENT = newClient();

    private static Client newClient() {
        Client client = ClientBuilder.newClient();
        client.register(JSONAttributesListMarshaller.class);
        return client;
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


        URI dicomWebURI = getUriInitParameter("online.kheops.pacs.uri");
        UriBuilder uriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies/{StudyInstanceUID}/series");
        URI uri = uriBuilder.build(studyInstanceUID);
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

        URI dicomWebURI = getUriInitParameter("online.kheops.pacs.uri");
        UriBuilder uriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies/{StudyInstanceUID}/metadata");
        URI uri = uriBuilder.build(studyInstanceUID);
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

    private URI getUriInitParameter(String param) {
        try {
            return new URI(context.getInitParameter(param));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Param " + param + " is not a valid URI", e);
        }
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteStudyFromInbox(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                         @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.deleteStudyFromInbox(callingUserPk, studyInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" from user:" + callingUserPk);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}")
    @Produces("application/dicom+json")
    public Response deleteSeriesFromInbox(@PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                          @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                          @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.deleteSeriesFromInbox(callingUserPk, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from user:" + callingUserPk);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response putSeriesInAlbum(@PathParam("album") Long albumPk,
                                     @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                     @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                     @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.putSeriesInAlbum(callingUserPk, albumPk, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+ "SeriesInstanceUID:"+seriesInstanceUID+" with albumPK "+albumPk);
        return Response.status(Response.Status.CREATED).build();

    }

    @PUT
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response putStudyInAlbum(@PathParam("album") Long albumPk,
                                    @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                    @QueryParam("album") Long fromAlbumPk,
                                    @QueryParam("inbox") Boolean fromInbox,
                                    @Context SecurityContext securityContext) {

        if ((fromAlbumPk != null && fromInbox != null)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Use only {album} or {inbox} not both").build();
        }

        if (fromInbox != null) {
            fromInbox = true;
        } else {
            fromInbox = false;
        }

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.putStudyInAlbum(callingUserPk, albumPk, studyInstanceUID, fromAlbumPk, fromInbox);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        LOG.info("finished sharing StudyInstanceUID:"+studyInstanceUID+" with albumPK "+albumPk);
        return Response.status(Response.Status.CREATED).build();
    }


    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response deleteStudyFromAlbum(@PathParam("album") Long albumPk,
                                         @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                         @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.deleteStudyFromAlbum(callingUserPk, albumPk, studyInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" from albumPK "+albumPk);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Secured
    @Path("studies/{StudyInstanceUID:([0-9]+[.])*[0-9]+}/series/{SeriesInstanceUID:([0-9]+[.])*[0-9]+}/albums/{album:[1-9][0-9]*}")
    public Response deleteSeriesFromAlbum(@PathParam("album") Long albumPk,
                                          @PathParam(Consts.StudyInstanceUID) String studyInstanceUID,
                                          @PathParam(Consts.SeriesInstanceUID) String seriesInstanceUID,
                                          @Context SecurityContext securityContext) {

        checkValidUID(studyInstanceUID, Consts.StudyInstanceUID);
        checkValidUID(seriesInstanceUID, Consts.SeriesInstanceUID);

        final long callingUserPk = ((KheopsPrincipal)securityContext.getUserPrincipal()).getDBID();

        try {
            Sending.deleteSeriesFromAlbum(callingUserPk, albumPk, studyInstanceUID, seriesInstanceUID);
        } catch(UserNotFoundException | AlbumNotFoundException | SeriesNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (AlbumForbiddenException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        }

        LOG.info("finished removing StudyInstanceUID:"+studyInstanceUID+" SeriesInstanceUID:"+seriesInstanceUID+" from albumPK "+albumPk);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
