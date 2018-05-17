package online.kheops.auth_server.resource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.dcm4che3.data.Tag;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlElement;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@Path("proxy")
public class CapabilitiesProxy {

    @Context
    ServletContext context;

    static class tokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        String expiresIn;
    }

    @POST
    @Path("capabilities/{capabilitySecret}/studies/{studyInstanceUID}")
    public Response STOWWithStudy(InputStream requestBody,
                         @PathParam("capabilitySecret") String capabilitySecret,
                         @PathParam("studyInstanceUID") String studyInstanceUID,
                         @HeaderParam("Content-Type") String contentType /*,
                     @Suspended AsyncResponse response*/) throws URISyntaxException {
        return Response.status(Response.Status.CREATED).build();

    }


    @POST
    @Path("capabilities/{capabilitySecret}/studies")
    public Response STOW(InputStream requestBody,
                     @PathParam("capabilitySecret") String capabilitySecret,
                     @HeaderParam("Content-Type") String contentType /*,
                     @Suspended AsyncResponse response*/) throws URISyntaxException {

        URI authenticationServerURI = new URI(context.getInitParameter("online.kheops.auth_server.uri"));
        URI dicomWebURI = new URI(context.getInitParameter("online.kheops.pacs.uri"));

        Form form = new Form();
        form.param("assertion", capabilitySecret).param("grant_type", "urn:x-kheops:params:oauth:grant-type:capability");

        UriBuilder uriBuilder = UriBuilder.fromUri(authenticationServerURI).path("token");
        URI uri = uriBuilder.build();
        Client client = ClientBuilder.newClient();
        tokenResponse tokenResponse = client.target(uri).request("application/json").post(Entity.form(form), tokenResponse.class);

        // get the user from the token
        DecodedJWT jwt = JWT.decode(tokenResponse.accessToken);
        String sub = jwt.getSubject();

        byte[] buffer = new byte[4096];
        StreamingOutput stream = os -> {
            int readBytes;
            while ((readBytes = requestBody.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
            os.close();
        };

        UriBuilder dicomWebUriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies");
        URI dicomWebUri = dicomWebUriBuilder.build();

        Entity<StreamingOutput> stowEntity = Entity.entity(stream, contentType);
        Client stowClient = ClientBuilder.newClient();
//        Response stowResponse = stowClient.target(dicomWebUri).request().post(stowEntity);



        // get the input stream
//        request



//        Client client = ClientBuilder.newClient();
//        tokenResponse tokenResponse = client.target(uri).request("application/json").post(Entity.form(form), tokenResponse.class);





        // get a token from the auth server
        return Response.status(Response.Status.CREATED).build();

    }

//
//
}
