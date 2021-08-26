package online.kheops.proxy.stow.authorization;

import online.kheops.proxy.id.ContentLocation;
import online.kheops.proxy.id.InstanceID;
import online.kheops.proxy.id.SeriesID;
import online.kheops.proxy.marshaller.VerifyResponse;
import online.kheops.proxy.part.Part;
import online.kheops.proxy.stow.GatewayException;
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
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.logging.Level.WARNING;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.*;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static online.kheops.proxy.stow.authorization.AuthorizationManagerException.Reason.SERIES_ACCESS_FORBIDDEN;
import static online.kheops.proxy.stow.authorization.AuthorizationManagerException.Reason.UNKNOWN_CONTENT_LOCATION;

public final class AuthorizationManager {
    private static final Logger LOG = Logger.getLogger(AuthorizationManager.class.getName());
    private static final Client CLIENT = ClientBuilder.newClient();

    private static final String HEADER_X_LINK_AUTHORIZATION = "X-Link-Authorization";

    private final Set<SeriesID> authorizedSeriesIDs = new HashSet<>();
    private final Set<SeriesID> forbiddenSeriesIDs = new HashSet<>();
    private final Set<InstanceID> forbiddenInstanceIDs = new HashSet<>();
    private final HashMap<InstanceID, List<String>> conflictMetadataInstanceIDs = new HashMap<>();
    private final Set<ContentLocation> authorizedContentLocations = new HashSet<>();
    private final UriBuilder authorizationUriBuilder;
    private final UriBuilder verifyUriBuilder;
    private final AuthorizationToken bearerToken;
    private final String headerXLinkAuthorization;

    private final List<ProcessingFailure> processingFailures = new ArrayList<>();

    private final MultivaluedMap<String, String> fileIDMap = new MultivaluedHashMap<>();

    private static final class ProcessingFailure {
        private final String fileID;

        ProcessingFailure(final String fileID) {
            this.fileID = fileID;
        }

        Optional<String> getFileID() {
            return Optional.ofNullable(fileID);
        }
    }

    public AuthorizationManager(URI authorizationServerRoot, AuthorizationToken authorizationToken, String albumId, String headerXLinkAuthorization) {
        this.bearerToken = Objects.requireNonNull(authorizationToken);
        this.headerXLinkAuthorization = headerXLinkAuthorization;
        authorizationUriBuilder = UriBuilder.fromUri(Objects.requireNonNull(authorizationServerRoot)).path("studies/{StudyInstanceUID}/series/{SeriesInstanceUID}");
        verifyUriBuilder = UriBuilder.fromUri(Objects.requireNonNull(authorizationServerRoot)).path("verifyInstance");
        if (albumId != null) {
            authorizationUriBuilder.path("/albums/" + albumId);
        }
    }

    // This method blocks while a connection is made to the authorization server
    // Throws an exception that describes the reason the authorization could not be acquired.
    // stores authorizations that have failed so that attributes can be patched
    // returns a set of InstanceID for which authorization has passed
    public Set<InstanceID> getAuthorization(Part part, String fileID) throws AuthorizationManagerException, GatewayException {
        Optional<ContentLocation> contentLocationOptional = part.getContentLocation();
        if (contentLocationOptional.isPresent()) {
            getAuthorization(contentLocationOptional.get());
            return Collections.emptySet();
        } else {
            Set<InstanceID> authorizedInstanceIDs = new HashSet<>();
            for (InstanceID instanceID: part.getInstanceIDs()) {
                if (fileID != null) {
                    fileIDMap.add(instanceID.getSOPInstanceUID(), fileID);
                }
                if (getAuthorization(instanceID)) {
                    authorizedInstanceIDs.add(instanceID);
                    authorizeContentLocations(part.getBulkDataLocations(instanceID));
                }
            }
            if (authorizedInstanceIDs.isEmpty()) {
                throw new AuthorizationManagerException(SERIES_ACCESS_FORBIDDEN);
            } else {
                return authorizedInstanceIDs;
            }
        }
    }

    public void addProcessingFailure(final String fileID) {
        processingFailures.add(new ProcessingFailure(fileID));
    }

    private static final Pattern STUDY_URI_PATTERN = Pattern.compile("^.*(/studies/([0-9]+[.])*[0-9]+)$");
    private static final int URI_PATTERN_GROUP = 1;
    private static final Pattern INSTANCE_URI_PATTERN = Pattern.compile("^.*(/studies/([0-9]+[.])*[0-9]+/series/([0-9]+[.])*[0-9]+/instances/([0-9]+[.])*[0-9]+)$");

    public Response getResponse(Attributes attributes, final int status) {

        final boolean linkAuthorization = headerXLinkAuthorization != null && headerXLinkAuthorization.equalsIgnoreCase("true");
        final URI rootURI = getRootUri();
        final UriBuilder retrieveULRBuilder;
        if (linkAuthorization) {
            retrieveULRBuilder = UriBuilder.fromUri(rootURI).path("/api/link/" + bearerToken.getToken());
        } else {
            retrieveULRBuilder = UriBuilder.fromUri(rootURI).path("/api");
        }

        if (attributes == null) {
            attributes = new Attributes(2);
            attributes.setString(Tag.RetrieveURL, VR.UR, "");
        } else {
            patchRetrieveUrl(attributes, STUDY_URI_PATTERN, retrieveULRBuilder);
        }

        Sequence referencedSOPSequence = attributes.getSequence(Tag.ReferencedSOPSequence);
        for (Attributes referencedSOP : (referencedSOPSequence != null ? referencedSOPSequence : Collections.<Attributes>emptyList())) {
            patchRetrieveUrl(referencedSOP, INSTANCE_URI_PATTERN, retrieveULRBuilder);
        }

        Sequence failedSOPs = attributes.getSequence(Tag.FailedSOPSequence);
        int size = forbiddenInstanceIDs.size() + conflictMetadataInstanceIDs.size();
        if (failedSOPs == null && size != 0) {
            failedSOPs = attributes.newSequence(Tag.FailedSOPSequence, size);
        }

        for (InstanceID forbiddenInstance: forbiddenInstanceIDs) {
            final String sopInstanceUID = forbiddenInstance.getSOPInstanceUID();
            Attributes failedAttributes = new Attributes(3);
            failedAttributes.setString(Tag.ReferencedSOPInstanceUID, VR.UI, forbiddenInstance.getSOPInstanceUID());
            failedAttributes.setString(Tag.ReferencedSOPClassUID, VR.UI, forbiddenInstance.getSOPClassUID());
            failedAttributes.setInt(Tag.FailureReason, VR.US, Status.NotAuthorized);

            if (fileIDMap.containsKey(sopInstanceUID) && !fileIDMap.get(sopInstanceUID).isEmpty()) {
                final String fileID = fileIDMap.getFirst(sopInstanceUID);
                fileIDMap.get(sopInstanceUID).remove(0);
                failedAttributes.setString(Tag.ReferencedFileID, VR.CS, fileID);
            }

            failedSOPs.add(failedAttributes);
        }

        for (InstanceID conflictMetadataInstance: conflictMetadataInstanceIDs.keySet()) {
            final String sopInstanceUID = conflictMetadataInstance.getSOPInstanceUID();
            Attributes failedAttributes = new Attributes(3);
            failedAttributes.setString(Tag.ReferencedSOPInstanceUID, VR.UI, conflictMetadataInstance.getSOPInstanceUID());
            failedAttributes.setString(Tag.ReferencedSOPClassUID, VR.UI, conflictMetadataInstance.getSOPClassUID());
            failedAttributes.setInt(Tag.FailureReason, VR.US, Status.InvalidAttributeValue);

            final String[] tagsArray = conflictMetadataInstanceIDs.get(conflictMetadataInstance).toArray(new String[conflictMetadataInstanceIDs.get(conflictMetadataInstance).size()]);
            failedAttributes.setValue(Tag.AttributeIdentifierList, VR.LO, tagsArray);

            if (fileIDMap.containsKey(sopInstanceUID) && !fileIDMap.get(sopInstanceUID).isEmpty()) {
                final String fileID = fileIDMap.getFirst(sopInstanceUID);
                fileIDMap.get(sopInstanceUID).remove(0);
                failedAttributes.setString(Tag.ReferencedFileID, VR.CS, fileID);
            }

            failedSOPs.add(failedAttributes);
        }

        Sequence otherFailures = attributes.getSequence(Tag.OtherFailuresSequence);
        if (otherFailures == null && !processingFailures.isEmpty()) {
            otherFailures = attributes.newSequence(Tag.OtherFailuresSequence, processingFailures.size());
        }
        for (ProcessingFailure processingFailure: processingFailures) {
            Attributes failureAttributes = new Attributes(1);
            failureAttributes.setInt(Tag.FailureReason, VR.US, Status.ProcessingFailure);
            Optional<String> fileID = processingFailure.getFileID();
            fileID.ifPresent(s -> failureAttributes.setString(Tag.ReferencedFileID, VR.CS, s));

            otherFailures.add(failureAttributes);
        }

        Response.Status responseStatus;
        switch (status) {
            case 200:
                responseStatus = OK;
                break;
            case 202:
                responseStatus = ACCEPTED;
                break;
            default:
                responseStatus = CONFLICT;
                break;
        }

        if (status == OK.getStatusCode() && (!forbiddenInstanceIDs.isEmpty() || !processingFailures.isEmpty() || !conflictMetadataInstanceIDs.isEmpty())) {
            responseStatus = ACCEPTED;
        }

        return Response.status(responseStatus).entity(attributes).build();
    }

    private void patchRetrieveUrl(Attributes attributes, Pattern pattern, UriBuilder uriBuilder) {
        final String retrieveUrl = attributes.getString(Tag.RetrieveURL, "");
        attributes.remove(Tag.RetrieveURL);
        final Matcher studyMatcher = pattern.matcher(retrieveUrl);
        if (studyMatcher.matches()) {
            final String patchedStudyUri = uriBuilder
                .path(studyMatcher.group(URI_PATTERN_GROUP))
                .build()
                .toString();
            attributes.setString(Tag.RetrieveURL, VR.UR, patchedStudyUri);
        }
    }

    private boolean getAuthorization(InstanceID instanceID) throws GatewayException {
        final SeriesID seriesID = instanceID.getSeriesID();
        if (authorizedSeriesIDs.contains(seriesID)) {
            return true;
        }
        if (forbiddenSeriesIDs.contains(seriesID)) {
            forbiddenInstanceIDs.add(instanceID);
            return false;
        }

        URI verifyUri = verifyUriBuilder.build();

        try (final Response response = CLIENT.target(verifyUri)
                .request()
                .header(AUTHORIZATION, bearerToken.getHeaderValue())
                .header(HEADER_X_LINK_AUTHORIZATION, headerXLinkAuthorization)
                .post(seriesID.getEntity())) {

            if (response.getStatusInfo().getFamily() == SUCCESSFUL) {
                VerifyResponse verifyResponse = response.readEntity(VerifyResponse.class);
                if (!verifyResponse.isValid()) {
                    if (verifyResponse.getHasSeriesAddAccess()) {
                        LOG.log(WARNING, () -> "Instances verification rejected for series:" + seriesID);
                        conflictMetadataInstanceIDs.put(instanceID, verifyResponse.getUnmatchingTags());
                        return false;
                    } else {
                        LOG.log(WARNING, () -> "Instances verification rejected for series:" + seriesID);
                        forbiddenInstanceIDs.add(instanceID);
                        return false;
                    }
                }
            } else {
                addProcessingFailure(fileIDMap.getFirst(instanceID.getSOPInstanceUID()));
                return false;
            }


        } catch (ProcessingException e) {
            forbiddenInstanceIDs.add(instanceID);
            throw new GatewayException("Error while verifying the instance", e);
        }  catch (WebApplicationException e) {
            LOG.log(WARNING, e, () -> "Unable to verify series using " + e.getResponse().getLocation());
            forbiddenInstanceIDs.add(instanceID);
            return false;
        }

        URI authorizationUri = authorizationUriBuilder.build(seriesID.getStudyUID(), seriesID.getSeriesUID());

        try (final Response response = CLIENT.target(authorizationUri)
                    .request()
                    .header(AUTHORIZATION, bearerToken.getHeaderValue())
                    .header(HEADER_X_LINK_AUTHORIZATION, headerXLinkAuthorization)
                    .put(Entity.text(""))) {

            if (response.getStatusInfo().getFamily() == SUCCESSFUL) {
                authorizedSeriesIDs.add(seriesID);
                return true;
            } else {
                String responseValue = response.readEntity(String.class);
                LOG.log(WARNING, () -> "Authorization server rejected authorization for series:" + seriesID +
                        " status:" + response.getStatus() + 
                        " response:" + responseValue);
                forbiddenSeriesIDs.add(seriesID);
                forbiddenInstanceIDs.add(instanceID);
                return false;
            }
        } catch (ProcessingException e) {
            forbiddenSeriesIDs.add(seriesID);
            forbiddenInstanceIDs.add(instanceID);
            throw new GatewayException("Error while getting the access token", e);
        }  catch (WebApplicationException e) {
            LOG.log(WARNING, e, () -> "Unable to get access to a series using " + e.getResponse().getLocation());
            forbiddenSeriesIDs.add(seriesID);
            forbiddenInstanceIDs.add(instanceID);
            return false;
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

    private URI getRootUri() {
        try {
            return new URI(System.getProperty("online.kheops.root.uri"));
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "Error with the STOWServiceURI", e);
            throw new WebApplicationException(INTERNAL_SERVER_ERROR);
        }
    }
}
