package online.kheops.auth_server.keycloak;

import online.kheops.auth_server.user.CacheUserName;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.user.UserResponse;
import online.kheops.auth_server.user.UserResponseBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.StringReader;
import java.net.URI;
import java.util.logging.Logger;

public class Keycloak {

    private static final String usersPath = "/users";

    private static URI usersUri;

    private static KeycloakToken token;

    private static final Logger LOG = Logger.getLogger(Keycloak.class.getName());

    private static Keycloak instance = null;

    private static CacheUserName cacheUserName;

    private Keycloak() throws KeycloakException{
        usersUri = UriBuilder.fromUri(KeycloakContextListener.getKeycloakAdminURI()).path(usersPath).build();
        token = KeycloakToken.getInstance();
        cacheUserName = CacheUserName.getInstance();
    }

    public static Keycloak getInstance() throws KeycloakException{
        if(instance == null) {
            instance = new Keycloak();
        }
        return instance;
    }

    public UserResponse getUser(String user)
            throws UserNotFoundException, KeycloakException {

        if(user.contains("@")) {
            final Response response;
            try {
                response = ClientBuilder.newClient().target(usersUri).queryParam("email", user.toLowerCase()).request().header(HttpHeaders.AUTHORIZATION, "Bearer "+token.getToken()).get();
            } catch (ProcessingException e) {
                throw new KeycloakException("Error during introspect token", e);
            }

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String output = response.readEntity(String.class);
                try(JsonReader jsonReader = Json.createReader(new StringReader(output))) {
                    JsonArray reply = jsonReader.readArray();
                    final KeycloakUsers keycloakUsers = new KeycloakUsers(reply);
                    if (keycloakUsers.size() > 0) {
                        final int index = keycloakUsers.verifyEmail(user);
                        return new UserResponseBuilder().setEmail(keycloakUsers.getEmail(index)).setSub(keycloakUsers.getId(0)).build();
                    } else {
                        throw new UserNotFoundException();
                    }
                }
            }

        } else {

            String userEmail = cacheUserName.getCachedValue(user);
            if(userEmail != null) {
                 return new UserResponseBuilder().setEmail(userEmail).setSub(user).build();
            }

            final URI userUri = UriBuilder.fromUri(usersUri).path("/"+user).build();
            final Response response;
            try {
                response =  ClientBuilder.newClient().target(userUri).request().header(HttpHeaders.AUTHORIZATION, "Bearer "+token.getToken()).get();
            } catch (ProcessingException e) {
                throw new KeycloakException("Error during introspect token", e);
            }
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String output = response.readEntity(String.class);
                output = "["+output+"]";
                try(JsonReader jsonReader = Json.createReader(new StringReader(output))) {
                    JsonArray reply = jsonReader.readArray();
                    final KeycloakUsers keycloakUser = new KeycloakUsers(reply);
                    if (keycloakUser.size() == 1) {
                        cacheUserName.cacheValue(keycloakUser.getId(0), keycloakUser.getEmail(0));
                        return new UserResponseBuilder().setEmail(keycloakUser.getEmail(0)).setSub(keycloakUser.getId(0)).build();
                    } else {
                        throw new UserNotFoundException();
                    }
                }
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new UserNotFoundException();
            }
        }
        throw new KeycloakException("ERROR:");
    }
}

