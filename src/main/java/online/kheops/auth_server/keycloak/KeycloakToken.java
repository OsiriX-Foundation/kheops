package online.kheops.auth_server.keycloak;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.StringReader;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

public class KeycloakToken {
    private static Client CLIENT = ClientBuilder.newClient();

    private URI tokenUri;
    private URI introspectUri;

    private String clientId;
    private String clientSecret;

    private final Form form;

    private JsonObject token;

    private static KeycloakToken instance = null;

    private KeycloakToken() throws KeycloakException{

        final Response response;
        try {
            response = CLIENT.target(KeycloakContextListener.getKeycloakOIDCConfigurationURI()).request().get();
        } catch (ProcessingException e) {
            throw new KeycloakException("Error during request OpenID Connect well-known", e);
        }

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            try (JsonReader jsonReader = Json.createReader(new StringReader(output))) {
                JsonObject wellKnownResponse = jsonReader.readObject();
                tokenUri = UriBuilder.fromUri(wellKnownResponse.getString("token_endpoint")).build();
                introspectUri = UriBuilder.fromUri(wellKnownResponse.getString("token_introspection_endpoint")).build();
            }
        } else{
            throw new KeycloakException("Error during request OpenID Connect well-known");
        }


        String username = KeycloakContextListener.getKeycloakUser();
        String password = KeycloakContextListener.getKeycloakPassword();
        clientId = KeycloakContextListener.getKeycloakClientId();
        clientSecret = KeycloakContextListener.getKeycloakClientSecret();

        form = new Form();
        form.param("grant_type", "password");
        form.param("username", username);
        form.param("password", password);
        form.param("client_id", clientId);
        form.param("client_secret", clientSecret);
    }

    public static synchronized KeycloakToken getInstance() throws KeycloakException{
        if (instance == null) {
            instance = new KeycloakToken();
        }
        return instance;
    }

    private void newToken() throws KeycloakException{

        final Response response;
        try {
            response = CLIENT.target(tokenUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(form));
        } catch (ProcessingException e) {
            throw new KeycloakException("Error while requesting a new token", e);
        }

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            try(JsonReader jsonReader = Json.createReader(new StringReader(output))) {
                token = jsonReader.readObject();
                return;
            }
        }
        throw new KeycloakException("Error while requesting a new token. Expected 200 but got : " + response.getStatus() +"___"+ response);
    }

    private void refreshToken() throws KeycloakException{

        final Form refreshTokenForm = new Form();
        refreshTokenForm.param("grant_type", "refresh_token");
        refreshTokenForm.param("refresh_token", getRefreshToken());
        refreshTokenForm.param("client_id", clientId);
        refreshTokenForm.param("client_secret", clientSecret);
        final Response response;
        try {
            response = CLIENT.target(tokenUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(refreshTokenForm));
        } catch (ProcessingException e) {
            throw new KeycloakException("Error while requesting a refresh token", e);
        }

        if (response.getStatusInfo().getFamily() != SUCCESSFUL) {
            throw new KeycloakException("Status of:" + response.getStatus() + " while getting a refresh token");
        }

        String output = response.readEntity(String.class);

        try(JsonReader jsonReader = Json.createReader(new StringReader(output))) {
            token = jsonReader.readObject();
        } catch (Exception e) {
            throw new KeycloakException("Error while parsing the refresh token response", e);
        }
    }

    public String getToken() throws KeycloakException {

        if (token != null) {

            if(introspect(getAccessToken())) {

                return getAccessToken();
            } else {
                if(introspect(getRefreshToken())) {
                    refreshToken();
                    return getAccessToken();

                } else {
                    newToken();
                    return getAccessToken();
                }
            }
        } else {
            newToken();
            return getAccessToken();
        }
    }

    private String getRefreshToken() { return token.getString("refresh_token"); }
    private String getAccessToken() { return token.getString("access_token"); }

    private boolean introspect(String token) throws KeycloakException{
        final Form introspectForm = new Form();
        introspectForm.param("token", token);
        introspectForm.param("client_id", clientId);
        introspectForm.param("client_secret", clientSecret);
        final Response response;
        try {
            response = CLIENT.target(introspectUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(introspectForm));
        } catch (ProcessingException e) {
            throw new KeycloakException("Error during token introspect", e);
        }

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            try(JsonReader jsonReader = Json.createReader(new StringReader(output))) {
                JsonObject result = jsonReader.readObject();
                return result.getBoolean("active");
            }
        }
        throw new KeycloakException("Error during token introspect");
    }

}
