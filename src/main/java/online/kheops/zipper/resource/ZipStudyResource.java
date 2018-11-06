package online.kheops.zipper.resource;

import online.kheops.zipper.token.AccessToken;
import online.kheops.zipper.token.AccessTokenType;
import online.kheops.zipper.token.BearerTokenRetriever;
import online.kheops.zipper.instance.Instance;
import online.kheops.zipper.instance.InstanceRetrievalService;
import online.kheops.zipper.InstanceZipper;
import online.kheops.zipper.marshaller.AttributesListMarshaller;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.Response.Status.*;

@Path("/studies")
public final class ZipStudyResource {
    private static final String ALBUM = "album";
    private static final String INBOX = "inbox";
    private static final String STUDY_INSTANCE_UID = "StudyInstanceUID";
    private static final String ASSERTION = "assertion";
    private static final String GRANT_TYPE = "grant_type";

    private static final Client CLIENT = newClient();
    private static final String DICOM_ZIP_FILENAME = "DICOM.ZIP";

    private static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
    }

    private static class Tokens {
        private final AccessToken accessToken;
        private final String bearerToken;

        private Tokens(AccessToken accessToken, String bearerToken) {
            this.accessToken = accessToken;
            this.bearerToken = bearerToken;
        }

        AccessToken getAccessToken() {
            return accessToken;
        }

        String getBearerToken() {
            return bearerToken;
        }
    }

    @Context
    ServletContext context;

    @GET
    @Path("/{" + STUDY_INSTANCE_UID +"}")
    @Produces("application/zip")
    public Response streamStudy(@PathParam(STUDY_INSTANCE_UID) String studyInstanceUID,
                                @HeaderParam(AUTHORIZATION) String authorizationHeader,
                                @QueryParam(ALBUM) Long fromAlbum,
                                @QueryParam(INBOX) Boolean fromInbox) {
        checkValidUID(studyInstanceUID, STUDY_INSTANCE_UID);

        final Tokens tokens = getTokens(getUserTokenFromHeader(authorizationHeader));
        final Set<Instance> instances = getInstances(tokens, studyInstanceUID, fromAlbum, fromInbox);
        final BearerTokenRetriever bearerTokenRetriever = new BearerTokenRetriever.Builder()
                .client(CLIENT)
                .authorizationURI(authorizationURI())
                .accessToken(tokens.getAccessToken())
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

    private String getUserTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new WebApplicationException(UNAUTHORIZED);
        }

        if (!authorizationHeader.toUpperCase().startsWith("BEARER ")) {
            throw new WebApplicationException(UNAUTHORIZED);
        }

        return authorizationHeader.substring(7);
    }

    private Tokens getTokens(String userToken) {
        Form capabilityForm = new Form().param(ASSERTION, userToken).param(GRANT_TYPE, AccessTokenType.CAPABILITY_TOKEN.getUrn());
        Form jwtForm = new Form().param(ASSERTION, userToken).param(GRANT_TYPE, AccessTokenType.JWT_BEARER_TOKEN.getUrn());

        URI tokenURI = UriBuilder.fromUri(authorizationURI()).path("/token").build();

        TokenResponse tokenResponse = null;
        AccessToken accessToken = null;

        try {
            tokenResponse = CLIENT.target(tokenURI).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.form(capabilityForm), TokenResponse.class);
            accessToken = AccessToken.getInstance(userToken, AccessTokenType.CAPABILITY_TOKEN);
        } catch (Exception ignored) {
            /* go on and try a different token type */
        }

        if (tokenResponse == null) {
            try {
                tokenResponse = CLIENT.target(tokenURI).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.form(jwtForm), TokenResponse.class);
                accessToken = AccessToken.getInstance(userToken, AccessTokenType.JWT_BEARER_TOKEN);
            } catch (WebApplicationException webException) {
                if (webException.getResponse().getStatus() == BAD_REQUEST.getStatusCode() ||
                        webException.getResponse().getStatus() == UNAUTHORIZED.getStatusCode()) {
                    throw new WebApplicationException(Response.status(UNAUTHORIZED).build());
                } else {
                    throw new WebApplicationException(BAD_GATEWAY);
                }
            } catch (Exception e) {
                throw new WebApplicationException(BAD_GATEWAY);
            }
        }

        return new Tokens(accessToken, tokenResponse.accessToken);
    }

    private Set<Instance> getInstances(final Tokens tokens,
                                       final String studyInstanceUID,
                                       final Long fromAlbum,
                                       final Boolean fromInbox) {
        final UriBuilder metadataUriBuilder = UriBuilder.fromUri(authorizationURI()).path("/studies/{studyInstanceUID}/metadata");
        if (fromAlbum != null) {
            metadataUriBuilder.queryParam(ALBUM, fromAlbum);
        }
        if (fromInbox != null) {
            metadataUriBuilder.queryParam(INBOX, fromInbox);
        }

        final URI metadataURI = metadataUriBuilder.build(studyInstanceUID);

        List<Attributes> attributesList;
        try {
            attributesList = CLIENT.target(metadataURI).request().accept("application/dicom+json").header(AUTHORIZATION, "Bearer " + tokens.getBearerToken()).get(new GenericType<List<Attributes>>() {});
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == NOT_FOUND.getStatusCode()) {
                throw new WebApplicationException(NOT_FOUND);
            } else {
                throw new WebApplicationException(BAD_GATEWAY);
            }
        } catch (Exception e) {
            throw new WebApplicationException(BAD_GATEWAY);
        }

        Set<Instance> instances = new HashSet<>();
        try {
            for (Attributes attr : attributesList) {
                Instance instance = Instance.newInstance(attr.getString(Tag.StudyInstanceUID), attr.getString(Tag.SeriesInstanceUID), attr.getString(Tag.SOPInstanceUID));
                instances.add(instance);
            }
        } catch (Exception e) {
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
