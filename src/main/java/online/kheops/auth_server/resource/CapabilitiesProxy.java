package online.kheops.auth_server.resource;

import com.auth0.jwt.JWT;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.SAXTransformer;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Path("proxy")
public class CapabilitiesProxy {

    @Context
    ServletContext context;

    @SuppressWarnings("unused")
    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        String expiresIn;
    }

    private enum Output {
        DICOM_XML {
            @Override
            StreamingOutput entity(final Attributes response) {
                return out -> {
                    try {
                        SAXTransformer.getSAXWriter(new StreamResult(out)).write(response);
                    } catch (Exception e) {
                        throw new WebApplicationException(errResponseAsTextPlain(e));
                    }
                };
            }
        },
        DICOM_JSON {
            @Override
            StreamingOutput entity(final Attributes response) {
                return out -> {
                    JsonGenerator gen = Json.createGenerator(out);
                    new JSONWriter(gen).write(response);
                    gen.flush();
                };
            }
        };

        abstract StreamingOutput entity(Attributes response);
    }

    private static Response errResponseAsTextPlain(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exceptionAsString).type("text/plain").build();
    }


    @POST
    @Path("/capabilities/{capabilitySecret}/studies")
    @Produces("application/dicom+xml")
    public Response storeInstancesXML(InputStream in,
                                      @PathParam("capabilitySecret") String capabilitySecret,
                                      @HeaderParam("Content-Type") String contentType) throws Exception {
        return store(in, contentType, capabilitySecret, null, Output.DICOM_XML);
    }

    @POST
    @Path("/capabilities/{capabilitySecret}/studies/{StudyInstanceUID}")
    @Produces("application/dicom+xml")
    public Response storeInstancesXML(InputStream in,
                                      @PathParam("capabilitySecret") String capabilitySecret,
                                      @HeaderParam("Content-Type") String contentType,
                                      @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception {
        return store(in, contentType, capabilitySecret, studyInstanceUID, Output.DICOM_XML);
    }

    @POST
    @Path("/capabilities/{capabilitySecret}/studies")
    @Produces("application/dicom+json")
    public Response storeInstancesJSON(InputStream in,
                                      @PathParam("capabilitySecret") String capabilitySecret,
                                      @HeaderParam("Content-Type") String contentType) throws Exception {
        return store(in, contentType, capabilitySecret, null, Output.DICOM_JSON);
    }

    @POST
    @Path("/capabilities/{capabilitySecret}/studies/{StudyInstanceUID}")
    @Produces("application/dicom+json")
    public Response storeInstancesJSON(InputStream in,
                                      @PathParam("capabilitySecret") String capabilitySecret,
                                      @HeaderParam("Content-Type") String contentType,
                                      @PathParam("StudyInstanceUID") String studyInstanceUID) throws Exception {
        return store(in, contentType, capabilitySecret, studyInstanceUID, Output.DICOM_JSON);
    }


    private Response store(InputStream requestBody, String contentType, String capabilitySecret, String studyInstanceUID, Output output)  throws Exception {
        Client client = ClientBuilder.newClient();
        URI authenticationServerURI = new URI(context.getInitParameter("online.kheops.auth_server.uri"));
        URI dicomWebURI = new URI(context.getInitParameter("online.kheops.pacs.uri"));

        Form form = new Form().param("assertion", capabilitySecret).param("grant_type", "urn:x-kheops:params:oauth:grant-type:capability");
        URI uri = UriBuilder.fromUri(authenticationServerURI).path("token").build();

        TokenResponse tokenResponse = client.target(uri).request("application/json").post(Entity.form(form), TokenResponse.class);

        // get the user from the token
        String sub = JWT.decode(tokenResponse.accessToken).getSubject();

        byte[] buffer = new byte[4096];
        StreamingOutput stream = os -> {
            int readBytes;
            while ((readBytes = requestBody.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
            os.close();
        };

        UriBuilder dicomWebUriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies");
        if (studyInstanceUID != null) {
            dicomWebUriBuilder = dicomWebUriBuilder.path("/" + studyInstanceUID);
        }
        URI dicomWebUri = dicomWebUriBuilder.build();

        InputStream is = client.target(dicomWebUri).request("application/dicom+json").post(Entity.entity(stream, contentType), InputStream.class);

        JsonParser parser = Json.createParser(is);
        JSONReader jsonReader = new JSONReader(parser);

        Attributes attributes = jsonReader.readDataset(null);
        Sequence instanceSequence = attributes.getSequence(Tag.ReferencedSOPSequence);

        Set<String> sentSeries = new HashSet<>();
        for (Attributes instance: instanceSequence) {
            String instanceURL = instance.getString(Tag.RetrieveURL);

            // this is a total hack, get the study and series UIDs out of the returned URL
            int studiesIndex = instanceURL.indexOf("/studies/");
            int seriesIndex = instanceURL.indexOf("/series/");
            int instancesIndex = instanceURL.indexOf("/instances/");

            String studyUID = instanceURL.substring(studiesIndex + 9, seriesIndex);
            String seriesUID = instanceURL.substring(seriesIndex + 8, instancesIndex);
            String combinedUID = studyUID + "/" + seriesUID;

            if (!sentSeries.contains(combinedUID)) {
                URI claimURI = UriBuilder.fromUri(authenticationServerURI).path("users/{user}/studies/{studyUID}/series/{seriesUID}").build(sub, studyUID, seriesUID);
                client.target(claimURI).request().header("Authorization", "Bearer " + tokenResponse.accessToken).put(Entity.text(""));

                sentSeries.add(combinedUID);
            }
        }

        return Response.ok(output.entity(attributes)).build();
    }
}
