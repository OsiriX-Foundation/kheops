package online.kheops.auth_server.keycloak;

import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.json.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.StringReader;
import java.net.URI;

import java.util.logging.Logger;

public class Keycloak {

    private static final String usersPath = "/users";

    private static boolean isInitialised = false;

    private static URI usersUri;

    private KeycloakToken token;

    private static final Logger LOG = Logger.getLogger(Keycloak.class.getName());

    public Keycloak() throws KeycloakException{
        if(!isInitialised) {
            initKeycloak();
        }
        token = new KeycloakToken();
    }

    private void initKeycloak() {
        usersUri = UriBuilder.fromUri(KeycloakContextListener.getKeycloakAdminURI()).path(usersPath).build();
        isInitialised = true;
    }

    public UserResponse getUser(String user) throws UserNotFoundException, KeycloakException{

        if(user.contains("@")) {

            Response response = ClientBuilder.newClient().target(usersUri).queryParam("email", user).request().header(HttpHeaders.AUTHORIZATION, "Bearer "+token.getToken()).get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String output = response.readEntity(String.class);
                JsonReader jsonReader = Json.createReader(new StringReader(output));
                JsonArray reply = jsonReader.readArray();
                final KeycloakUsers keycloakUsers = new KeycloakUsers(reply);
                if(keycloakUsers.size() > 0) {
                    final int index = keycloakUsers.verifyEmail(user);
                    return new UserResponseBuilder().setEmail(keycloakUsers.getEmail(index)).setSub(keycloakUsers.getId(0)).build();
                } else {
                    throw new UserNotFoundException();
                }
            }

        } else {
            final URI userUri = UriBuilder.fromUri(usersUri).path("/"+user).build();
            Response response =  ClientBuilder.newClient().target(userUri).request().header(HttpHeaders.AUTHORIZATION, "Bearer "+token.getToken()).get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String output = response.readEntity(String.class);
                output = "["+output+"]";
                JsonReader jsonReader = Json.createReader(new StringReader(output));
                JsonArray reply = jsonReader.readArray();
                final KeycloakUsers keycloakUser = new KeycloakUsers(reply);
                if(keycloakUser.size() == 1) {
                    return new UserResponseBuilder().setEmail(keycloakUser.getEmail(0)).setSub(keycloakUser.getId(0)).build();
                } else {
                    throw new UserNotFoundException();
                }
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new UserNotFoundException();
            }
        }
        throw new KeycloakException("ERROR:");

    }

}


