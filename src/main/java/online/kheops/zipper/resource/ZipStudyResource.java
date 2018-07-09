package online.kheops.zipper.resource;

import org.dcm4che3.data.Attributes;
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
import java.util.List;

@Path("/studies")
public class ZipStudyResource {

    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        String expiresIn;
        @XmlElement(name = "user")
        String user;
    }


    @Context
    ServletContext context;

    @GET
    @Path("/simple")
    public Response simple() {
        return Response.status(Response.Status.OK).build();
    }

        @GET
    @Path("/{StudyInstanceUID}/stream")
    @Produces("application/zip")
    public Response streamStudy(@PathParam("StudyInstanceUID") String studyInstanceUID, @HeaderParam("authorization") String authorizationHeader) {

        // get the token from the user, and figure out what kind of token it is.
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

        // try to get the token as
        URI dicomWebURI = dicomWebURI();
        URI authorizationURI = authorizationURI();

        URI tokenURI = UriBuilder.fromUri(authorizationURI).path("/token").build();

        Client client = ClientBuilder.newClient();

        TokenResponse tokenResponse = null;

        try {
            tokenResponse = client.target(tokenURI).request("application/json").post(Entity.form(capabilityForm), TokenResponse.class);
        } catch (Exception e) {
            /* empty */
        }

        if (tokenResponse == null) {
            try {
                tokenResponse = client.target(tokenURI).request("application/json").post(Entity.form(jwtForm), TokenResponse.class);
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        // metadata URI
        URI metadataURI = UriBuilder.fromUri(authorizationURI).path("/users/{user}/studies/{studyInstanceUID}/metadata").build(tokenResponse.user, studyInstanceUID);

        List<Attributes> studyList = client.target(metadataURI).request().accept("application/dicom+json").header("Authorization", "Bearer "+tokenResponse.accessToken).get(new GenericType<List<Attributes>>() {});



        return Response.status(Response.Status.OK).build();


        // so now we have a token, try to get









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

    private void checkValidUID(String uid, String name) {
        try {
            new Oid(uid);
        } catch (GSSException exception) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(name + " is not a valid UID").build());
        }
    }
}
