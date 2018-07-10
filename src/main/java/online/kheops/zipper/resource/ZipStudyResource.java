package online.kheops.zipper.resource;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Path("/studies")
public class ZipStudyResource {

    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "user")
        String user;
    }

    static class Instance {
        String StudyInstanceUID;
        String SeriesInstanceUID;
        String SOPInstanceUID;
    }

    @Context
    ServletContext context;

    @GET
    @Path("/{StudyInstanceUID}/stream")
    @Produces("application/zip")
    public Response streamStudy(@PathParam("StudyInstanceUID") String studyInstanceUID, @HeaderParam("authorization") String authorizationHeader) {

        if (authorizationHeader == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (!authorizationHeader.toUpperCase().startsWith("BEARER ")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        checkValidUID(studyInstanceUID, "studyInstanceUID");

        final String userToken = authorizationHeader.substring(7);

        Form capabilityForm = new Form().param("assertion", userToken).param("grant_type", "urn:x-kheops:params:oauth:grant-type:capability").param("return_user", "true");
        Form jwtForm = new Form().param("assertion", userToken).param("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer").param("return_user", "true");

        URI dicomWebURI = dicomWebURI();
        URI authorizationURI = authorizationURI();

        URI tokenURI = UriBuilder.fromUri(authorizationURI).path("/token").build();

        final Client client = ClientBuilder.newClient();
        client.register(AttributesListMarshaller.class);

        TokenResponse tokenResponse = null;
        Form authorizationForm = null;

        try {
            tokenResponse = client.target(tokenURI).request("application/json").post(Entity.form(capabilityForm), TokenResponse.class);
            authorizationForm = capabilityForm;
        } catch (Exception e) {
            /* empty */
        }

        if (tokenResponse == null) {
            try {
                tokenResponse = client.target(tokenURI).request("application/json").post(Entity.form(jwtForm), TokenResponse.class);
                authorizationForm = jwtForm;
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        URI metadataURI = UriBuilder.fromUri(authorizationURI).path("/users/{user}/studies/{studyInstanceUID}/metadata").build(tokenResponse.user, studyInstanceUID);

        List<Attributes> attributesList = client.target(metadataURI).request().accept("application/dicom+json").header("Authorization", "Bearer "+tokenResponse.accessToken).get(new GenericType<List<Attributes>>() {});

        Set<Instance> instanceList = new HashSet<>();
        for (Attributes attr: attributesList) {
            Instance instance = new Instance();
            instance.StudyInstanceUID = attr.getString(Tag.StudyInstanceUID);
            instance.SeriesInstanceUID = attr.getString(Tag.SeriesInstanceUID);
            instance.SOPInstanceUID = attr.getString(Tag.SOPInstanceUID);
            instanceList.add(instance);
        }

        UriBuilder instanceURIBuilder = UriBuilder.fromUri(dicomWebURI).path("/wado")
                .queryParam("requestType", "WADO")
                .queryParam("contentType", "application%2Fdicom")
                .queryParam("transferSyntax", "*");

        authorizationForm.asMap().remove("return_user");

        final Form theForm = authorizationForm;
        StreamingOutput stream = os -> {
            ZipOutputStream zipStream = new ZipOutputStream(os);

            for (Instance instance : instanceList) {
                theForm.asMap().remove("scope");
                Form scopedForm = theForm.param("scope", "StudyInstanceUID=" + instance.StudyInstanceUID + " SeriesInstanceUID=" + instance.SeriesInstanceUID);

                TokenResponse scopedResponse;
                try {
                    scopedResponse = client.target(tokenURI).request("application/json").post(Entity.form(scopedForm), TokenResponse.class);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
                URI instanceURI = instanceURIBuilder.replaceQueryParam("studyUID", instance.StudyInstanceUID)
                        .replaceQueryParam("seriesUID", instance.SeriesInstanceUID)
                        .replaceQueryParam("objectUID", instance.SOPInstanceUID)
                        .build();

                byte[] instanceArray;
                try {
                    instanceArray = client.target(instanceURI).request("application/dicom").header("Authorization", "Bearer " + scopedResponse.accessToken).get(byte[].class);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }

                ZipEntry e = new ZipEntry(instance.SOPInstanceUID + ".dcm");
                zipStream.putNextEntry(e);
                zipStream.write(instanceArray);
                zipStream.closeEntry();
            }
            zipStream.close();
        };

        return Response.ok(stream).build();

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
