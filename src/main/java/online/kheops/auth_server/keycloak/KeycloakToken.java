package online.kheops.auth_server.keycloak;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.StringReader;
import java.net.URI;

public class KeycloakToken {

    private final static String basePath1 = "/auth/realms";
    private final static String basePath2 = "/protocol/openid-connect";
    private final static String tokenPath = "/token";
    private final static String introspectPath = "/token/introspect";

    private static URI tokenUri;
    private static URI introspectUri;

    private static String USERNAME;
    private static String PASSWORD;
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;

    private static final Form form = new Form();

    private static boolean isInitialised = false;

    private static JsonObject token;

    public KeycloakToken() {

        if(!isInitialised) {

            tokenUri = UriBuilder.fromUri(KeycloakContextListener.getKeycloakUri()).path(basePath1+"/"+KeycloakContextListener.getKeycloakRealms()+basePath2+tokenPath).build();
            introspectUri = UriBuilder.fromUri(KeycloakContextListener.getKeycloakUri()).path(basePath1+"/"+KeycloakContextListener.getKeycloakRealms()+basePath2+introspectPath).build();

            USERNAME = KeycloakContextListener.getKeycloakUser();
            PASSWORD = KeycloakContextListener.getKeycloakPassword();
            CLIENT_ID = KeycloakContextListener.getKeycloakClientId();
            CLIENT_SECRET = KeycloakContextListener.getKeycloakClientSecret();

            form.param("grant_type", "password");
            form.param("username", USERNAME);
            form.param("password", PASSWORD);
            form.param("client_id", CLIENT_ID);
            form.param("client_secret", CLIENT_SECRET);
            isInitialised = true;
        }
    }

    private void newToken() throws KeycloakException{

        Response response = ClientBuilder.newClient().target(tokenUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(form));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            JsonReader jsonReader = Json.createReader(new StringReader(output));
            token = jsonReader.readObject();
            return;
        }
        throw new KeycloakException("Error during request a new token");
    }

    private void refreshToken() throws KeycloakException{

        Response response = ClientBuilder.newClient().target(tokenUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(form));

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

    private String getRefreshToken() {
        return token.getString("refresh_token");
    }
    private String getAccessToken() { return token.getString("access_token"); }

    private boolean introspect(String token) {
        final Form form = new Form();
        form.param("token", token);
        form.param("client_id", CLIENT_ID);
        form.param("client_secret", CLIENT_SECRET);
        Response response = ClientBuilder.newClient().target(introspectUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(form));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            JsonReader jsonReader = Json.createReader(new StringReader(output));
            JsonObject result = jsonReader.readObject();
            boolean active = result.getBoolean("active");
            return active;
        }
        return false;
    }

}
