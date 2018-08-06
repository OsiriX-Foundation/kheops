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

@Path("/studies")
public final class ZipStudyResource {

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
    @Path("/{StudyInstanceUID}/stream")
    @Produces("application/zip")
    public Response streamStudy(@PathParam("StudyInstanceUID") String studyInstanceUID, @HeaderParam("authorization") String authorizationHeader) {
        checkValidUID(studyInstanceUID, "studyInstanceUID");

        final Tokens tokens = getTokens(getUserTokenFromHeader(authorizationHeader));
        final Set<Instance> instances = getInstances(tokens, studyInstanceUID);
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + DICOM_ZIP_FILENAME + "\"")
                .build();
    }

    private static Client newClient() {
        final Client client = ClientBuilder.newClient();
        client.register(AttributesListMarshaller.class);
        return client;
    }

    private String getUserTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        if (!authorizationHeader.toUpperCase().startsWith("BEARER ")) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        return authorizationHeader.substring(7);
    }

    private Tokens getTokens(String userToken) {
        Form capabilityForm = new Form().param("assertion", userToken).param("grant_type", AccessTokenType.CAPABILITY_TOKEN.getUrn());
        Form jwtForm = new Form().param("assertion", userToken).param("grant_type", AccessTokenType.JWT_BEARER_TOKEN.getUrn());

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
                if (webException.getResponse().getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
                    throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
                } else {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).build());
                }
            } catch (Exception e) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).build());
            }
        }

        return new Tokens(accessToken, tokenResponse.accessToken);
    }

    private Set<Instance> getInstances(Tokens tokens, String studyInstanceUID) {
        final URI metadataURI = UriBuilder.fromUri(authorizationURI()).path("/studies/{studyInstanceUID}/metadata").build(studyInstanceUID);

        List<Attributes> attributesList;
        try {
            attributesList = CLIENT.target(metadataURI).request().accept("application/dicom+json").header("Authorization", "Bearer " + tokens.getBearerToken()).get(new GenericType<List<Attributes>>() {});
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            } else {
                throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).build());
            }
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).build());
        }

        Set<Instance> instances = new HashSet<>();
        try {
            for (Attributes attr : attributesList) {
                Instance instance = Instance.newInstance(attr.getString(Tag.StudyInstanceUID), attr.getString(Tag.SeriesInstanceUID), attr.getString(Tag.SOPInstanceUID));
                instances.add(instance);
            }
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_GATEWAY).build());
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
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(name + " is not a valid UID").build());
        }
    }
}
