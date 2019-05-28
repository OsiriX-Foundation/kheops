package online.kheops.zipper.resource;

import online.kheops.zipper.accesstoken.AccessToken;
import online.kheops.zipper.bearertoken.BearerTokenRetriever;
import online.kheops.zipper.instance.Instance;
import online.kheops.zipper.instance.InstanceRetrievalService;
import online.kheops.zipper.InstanceZipper;
import online.kheops.zipper.marshaller.AttributesListMarshaller;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.Response.Status.*;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_PASSWORD;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.HTTP_AUTHENTICATION_USERNAME;

@Path("/studies")
public final class ZipStudyResource {
    private static final Logger LOG = Logger.getLogger(ZipStudyResource.class.getName());

    private static final String ALBUM = "album";
    private static final String INBOX = "inbox";
    private static final String STUDY_INSTANCE_UID = "StudyInstanceUID";

    private static final Client CLIENT = newClient().register(HttpAuthenticationFeature.basicBuilder().build());
    private static final String DICOM_ZIP_FILENAME = "DICOM.ZIP";

    @Context
    ServletContext context;

    @GET
    @Path("/{" + STUDY_INSTANCE_UID +"}")
    @Produces("application/zip")
    public Response streamStudy(@PathParam(STUDY_INSTANCE_UID) String studyInstanceUID,
                                @HeaderParam(AUTHORIZATION) String authorizationHeader,
                                @QueryParam(ALBUM) String fromAlbum,
                                @QueryParam(INBOX) Boolean fromInbox) {
        checkValidUID(studyInstanceUID, STUDY_INSTANCE_UID);

        final AccessToken accessToken = AccessToken.fromAuthorizationHeader(authorizationHeader);
        final Set<Instance> instances = getInstances(accessToken, studyInstanceUID, fromAlbum, fromInbox);
        final BearerTokenRetriever bearerTokenRetriever = new BearerTokenRetriever.Builder()
                .client(CLIENT)
                .authorizationURI(authorizationURI())
                .accessToken(accessToken)
                .build();
        final InstanceRetrievalService instanceRetrievalService = new InstanceRetrievalService.Builder()
                .client(CLIENT)
                .wadoURI(dicomWebURI())
                .bearerTokenRetriever(bearerTokenRetriever)
                .instances(instances)
                .build();

        return Response.ok(InstanceZipper.newInstance(instanceRetrievalService).getStreamingOutput())
                .header(CONTENT_DISPOSITION, "attachment; filename=\"" + DICOM_ZIP_FILENAME + "\"")
                .build();
    }

    private static Client newClient() {
        final Client client = ClientBuilder.newClient();
        client.register(AttributesListMarshaller.class);
        return client;
    }

    private Set<Instance> getInstances(final AccessToken accessToken,
                                       final String studyInstanceUID,
                                       final String fromAlbum,
                                       final Boolean fromInbox) {
        final UriBuilder metadataUriBuilder = UriBuilder.fromUri(authorizationURI()).path("/studies/{studyInstanceUID}/metadata");
        if (fromAlbum != null) {
            metadataUriBuilder.queryParam(ALBUM, fromAlbum);
        }
        if (fromInbox != null) {
            metadataUriBuilder.queryParam(INBOX, fromInbox);
        }

        final URI metadataURI = metadataUriBuilder.build(studyInstanceUID);

        CLIENT.property(HTTP_AUTHENTICATION_USERNAME, context.getInitParameter("online.client.zipperclientid"));
        CLIENT.property(HTTP_AUTHENTICATION_PASSWORD, context.getInitParameter("online.client.zippersecret"));

        List<Attributes> attributesList;
        try {
            attributesList = CLIENT.target(metadataURI).request().accept("application/dicom+json").header(AUTHORIZATION, "Bearer " + accessToken.toString()).get(new GenericType<List<Attributes>>() {});
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
            } else {
                LOG.log(SEVERE, "Bad Gateway", e);
                throw new WebApplicationException(BAD_GATEWAY);
            }
        } catch (ProcessingException e) {
            LOG.log(SEVERE, "Processing Error", e);
            throw new WebApplicationException(BAD_GATEWAY);
        }

        Set<Instance> instances = new HashSet<>();
        try {
            for (Attributes attr : attributesList) {
                Instance instance = Instance.newInstance(attr.getString(Tag.StudyInstanceUID), attr.getString(Tag.SeriesInstanceUID), attr.getString(Tag.SOPInstanceUID));
                instances.add(instance);
            }
        } catch (Exception e) {
            LOG.log(SEVERE, "Parsing Error", e);
            throw new WebApplicationException(BAD_GATEWAY);
        }

        return instances;
    }

    private URI dicomWebURI() {
        URI dicomWebURI;
        try {
            dicomWebURI = new URI(context.getInitParameter("online.kheops.pacs.uri"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("online.kheops.pacs.uri is not a valid URI", e);
        }
        return dicomWebURI;
    }

    private URI authorizationURI() {
        URI authorizationURI;
        try {
            authorizationURI = new URI(context.getInitParameter("online.kheops.auth_server.uri"));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("online.kheops.auth_server.uri is not a valid URI", e);
        }
        return authorizationURI;
    }

    private void checkValidUID(String uid, @SuppressWarnings("SameParameterValue") String name) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new WebApplicationException(name + " is not a valid UID", BAD_REQUEST);
        }
    }
}
