package online.kheops.auth_server.keycloak;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.StringReader;
import java.net.URI;

public class KeycloakToken {

    private static URI tokenUri;
    private static URI introspectUri;

    private static String USERNAME;
    private static String PASSWORD;
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;

    private static final Form form = new Form();

    private static JsonObject token;

    private static KeycloakToken instance = null;

    private KeycloakToken() throws KeycloakException{

        final Response response;
        try {
            response = ClientBuilder.newClient().target(KeycloakContextListener.getKeycloakWellKnownURI()).request().get();
        } catch (ProcessingException e) {
            throw new KeycloakException("Error during request OpenID Connect well-known", e);
        }

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            JsonReader jsonReader = Json.createReader(new StringReader(output));
            JsonObject wellKnownResponse = jsonReader.readObject();
            tokenUri = UriBuilder.fromUri(wellKnownResponse.getString("token_endpoint")).build();
            introspectUri = UriBuilder.fromUri(wellKnownResponse.getString("token_introspection_endpoint")).build();
        } else {
            throw new KeycloakException("Error during request OpenID Connect well-known");
        }


        USERNAME = KeycloakContextListener.getKeycloakUser();
        PASSWORD = KeycloakContextListener.getKeycloakPassword();
        CLIENT_ID = KeycloakContextListener.getKeycloakClientId();
        CLIENT_SECRET = KeycloakContextListener.getKeycloakClientSecret();

        form.param("grant_type", "password");
        form.param("username", USERNAME);
        form.param("password", PASSWORD);
        form.param("client_id", CLIENT_ID);
        form.param("client_secret", CLIENT_SECRET);
    }

    public static KeycloakToken getInstance() throws KeycloakException{
        if (instance == null) {
            instance = new KeycloakToken();
        }
        return instance;
    }

    private void newToken() throws KeycloakException{

        final Response response;
        try {
            response = ClientBuilder.newClient().target(tokenUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(form));
        } catch (ProcessingException e) {
            throw new KeycloakException("Error during request a new token", e);
        }

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            JsonReader jsonReader = Json.createReader(new StringReader(output));
            token = jsonReader.readObject();
            return;
        }
        throw new KeycloakException("Error during request a new token");
    }

    private void refreshToken() throws KeycloakException{

        final Form refreshTokenForm = new Form();
        refreshTokenForm.param("grant_type", "refresh_token");
        refreshTokenForm.param("refresh_token", getRefreshToken());
        refreshTokenForm.param("client_id", CLIENT_ID);
        refreshTokenForm.param("client_secret", CLIENT_SECRET);
        final Response response;
        try {
            response = ClientBuilder.newClient().target(tokenUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(refreshTokenForm));
        } catch (ProcessingException e) {
            throw new KeycloakException("Error during request a refresh token", e);
        }

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            JsonReader jsonReader = Json.createReader(new StringReader(output));
            token = jsonReader.readObject();
        }
        throw new KeycloakException("Error during request a refresh token");

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
        introspectForm.param("client_id", CLIENT_ID);
        introspectForm.param("client_secret", CLIENT_SECRET);
        final Response response;
        try {
            response = ClientBuilder.newClient().target(introspectUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(introspectForm));
        } catch (ProcessingException e) {
            throw new KeycloakException("Error during introspect token", e);
        }

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            JsonReader jsonReader = Json.createReader(new StringReader(output));
            JsonObject result = jsonReader.readObject();
            return result.getBoolean("active");
        }
        throw new KeycloakException("Error during introspect token");
    }

}
