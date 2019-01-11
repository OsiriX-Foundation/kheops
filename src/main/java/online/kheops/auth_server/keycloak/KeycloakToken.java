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

    private final static URI tokenUri = UriBuilder.fromUri("https://keycloak.kheops.online").path("/auth/realms/StaticLoginConnect/protocol/openid-connect/token").build();
    private final static URI introspectUri = UriBuilder.fromUri("https://keycloak.kheops.online").path("/auth/realms/StaticLoginConnect/protocol/openid-connect/token/introspect").build();

    private static final String USERNAME = "XXX";
    private static final String PASSWORD = "XXX";
    private static final String CLIENT_ID = "XXX";
    private static final String CLIENT_SECRET = "XXX";


    private static JsonObject token;

    public KeycloakToken() { }

    private boolean newToken() {
        final Form form = new Form();
        form.param("grant_type", "password");
        form.param("username", USERNAME);
        form.param("password", PASSWORD);
        form.param("client_id", CLIENT_ID);
        form.param("client_secret", CLIENT_SECRET);
        Response response = ClientBuilder.newClient().target(tokenUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(form));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            JsonReader jsonReader = Json.createReader(new StringReader(output));
            token = jsonReader.readObject();
            return true;
        }
        return false;
    }

    private boolean refreshToken() {
        final Form form = new Form();
        form.param("grant_type", "refresh_token");
        form.param("refresh_token", getRefreshToken());
        form.param("client_id", CLIENT_ID);
        form.param("client_secret", CLIENT_SECRET);
        Response response = ClientBuilder.newClient().target(tokenUri).request().header("Content-Type", "application/x-www-form-urlencoded").post(Entity.form(form));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String output = response.readEntity(String.class);
            JsonReader jsonReader = Json.createReader(new StringReader(output));
            token = jsonReader.readObject();
            return true;
        }
        return false;
    }

    public String getToken() {

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
    private String getAccessToken() {
        return token.getString("access_token");
    }

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
