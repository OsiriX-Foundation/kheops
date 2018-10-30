package online.kheops.proxy.stow.authorization;

import online.kheops.proxy.id.ContentLocation;
import online.kheops.proxy.id.InstanceID;
import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.part.MissingAttributeException;
import online.kheops.proxy.part.Part;
import online.kheops.proxy.stow.GatewayException;
import online.kheops.proxy.stow.resource.Resource;
import online.kheops.proxy.tokens.AuthorizationToken;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Status;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.SEVERE;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.OK;
import static online.kheops.proxy.stow.authorization.AuthorizationManagerException.Reason.SERIES_ACCESS_FORBIDDEN;
import static online.kheops.proxy.stow.authorization.AuthorizationManagerException.Reason.UNKNOWN_CONTENT_LOCATION;

public final class AuthorizationManager {
    private static final Logger LOG = Logger.getLogger(Resource.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient();

    private final Set<SeriesID> authorizedSeriesIDs = new HashSet<>();
    private final Set<SeriesID> forbiddenSeriesIDs = new HashSet<>();
    private final Set<InstanceID> forbiddenInstanceIDs = new HashSet<>();
    private final Set<ContentLocation> authorizedContentLocations = new HashSet<>();
    private final UriBuilder authorizationUriBuilder;
    private final UriBuilder fetchUriBuilder;
    private final AuthorizationToken bearerToken;

    public AuthorizationManager(URI authorizationServerRoot, AuthorizationToken authorizationToken, String albumId, String studyInstanceUID) {
        this.bearerToken = Objects.requireNonNull(authorizationToken);
        authorizationUriBuilder = UriBuilder.fromUri(Objects.requireNonNull(authorizationServerRoot)).path("studies/{StudyInstanceUID}/series/{SeriesInstanceUID}");
        if (albumId != null) {
            authorizationUriBuilder.path("/albums/" + albumId);
        }
        fetchUriBuilder = UriBuilder.fromUri(Objects.requireNonNull(authorizationServerRoot)).path("studies/{StudyInstanceUID}/fetch");
    }

    // This method blocks while a connection is made to the authorization server
    // Throws an exception that describes the reason the authorization could not be acquired.
    // stores authorizations that have failed so that attributes can be patched
    public void getAuthorization(Part part) throws AuthorizationManagerException, GatewayException {
        try {
            Optional<InstanceID> instanceIDOptional = part.getInstanceID();
            if (instanceIDOptional.isPresent()) {
                getAuthorization(instanceIDOptional.get());
            }
        } catch (MissingAttributeException e) {
            throw new AuthorizationManagerException("Unable to get instance", AuthorizationManagerException.Reason.MISSING_ATTRIBUTE, e);
        }
        Optional<ContentLocation> contentLocationOptional = part.getContentLocation();
        if (contentLocationOptional.isPresent()) {
            getAuthorization(contentLocationOptional.get());
        }

        authorizeContentLocations(part.getBulkDataLocations());
    }

    public Response getResponse(final Attributes attributes, final Response.Status status) {
        if (attributes == null) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        Sequence failedSOPs = attributes.getSequence(Tag.FailedSOPSequence);
        if (failedSOPs == null && !forbiddenInstanceIDs.isEmpty()) {
            failedSOPs = attributes.newSequence(Tag.FailedSOPSequence, forbiddenInstanceIDs.size());
        }

        for (InstanceID forbiddenInstance: forbiddenInstanceIDs) {
            Attributes failedAttributes = new Attributes(3);
            failedAttributes.setString(Tag.ReferencedSOPInstanceUID, VR.UI, forbiddenInstance.getSOPInstanceUID());
            failedAttributes.setString(Tag.ReferencedSOPClassUID, VR.UI, forbiddenInstance.getSOPClassUID());
            failedAttributes.setInt(Tag.FailureReason, VR.US, Status.NotAuthorized);

            failedSOPs.add(failedAttributes);
        }

        authorizedSeriesIDs.stream()
                .map(SeriesID::getStudyUID)
                .collect(Collectors.toSet())
                .forEach(this::triggerFetch);

        Response.Status responseStatus = status;
        if (status.equals(OK) && !forbiddenInstanceIDs.isEmpty()) {
            responseStatus = ACCEPTED;
        }

        return Response.status(responseStatus).entity(attributes).build();
    }

    private void getAuthorization(InstanceID instanceID) throws AuthorizationManagerException, GatewayException {
        final SeriesID seriesID = instanceID.getSeriesID();
        if (authorizedSeriesIDs.contains(seriesID)) {
            return;
        }
        if (forbiddenSeriesIDs.contains(seriesID)) {
            forbiddenInstanceIDs.add(instanceID);
            throw new AuthorizationManagerException(SERIES_ACCESS_FORBIDDEN);
        }

        URI uri = authorizationUriBuilder.build(seriesID.getStudyUID(), seriesID.getSeriesUID());

        final Response response;
        try {
            response = CLIENT.target(uri)
                    .request()
                    .header(AUTHORIZATION, bearerToken.getHeaderValue())
                    .put(Entity.text(""));
        } catch (ProcessingException e) {
            forbiddenSeriesIDs.add(seriesID);
            forbiddenInstanceIDs.add(instanceID);
            throw new GatewayException("Error while getting the access token", e);
        }  catch (WebApplicationException e) {
            forbiddenSeriesIDs.add(seriesID);
            forbiddenInstanceIDs.add(instanceID);
            throw new AuthorizationManagerException(SERIES_ACCESS_FORBIDDEN, e);
        }

        if (response.getStatusInfo().getFamily() == SUCCESSFUL) {
            authorizedSeriesIDs.add(seriesID);
        } else {
            forbiddenSeriesIDs.add(seriesID);
            forbiddenInstanceIDs.add(instanceID);
            throw new AuthorizationManagerException(SERIES_ACCESS_FORBIDDEN);
        }
    }

    private void getAuthorization(ContentLocation contentLocation) throws AuthorizationManagerException{
        if (!authorizedContentLocations.contains(contentLocation)) {
            throw new AuthorizationManagerException(UNKNOWN_CONTENT_LOCATION);
        }
    }

    private void authorizeContentLocations(Set<ContentLocation> contentLocations) {
        authorizedContentLocations.addAll(contentLocations);
    }

    private void triggerFetch(String studyInstanceUID) {
        URI uri = fetchUriBuilder.build(studyInstanceUID);

        try {
            Response response = CLIENT.target(uri)
                    .request()
                    .header(AUTHORIZATION, bearerToken.getHeaderValue())
                    .post(Entity.text(""));
            if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
                LOG.log(SEVERE, () -> "Error while triggering fetch for studyInstanceUID:" + studyInstanceUID + "status code:" + response.getStatus());
            }
        } catch (ProcessingException | WebApplicationException e) {
            LOG.log(SEVERE, "Error while triggering fetch for studyInstanceUID:" + studyInstanceUID, e);
        }
    }
}
