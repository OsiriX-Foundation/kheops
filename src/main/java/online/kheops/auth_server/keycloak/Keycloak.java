package online.kheops.auth_server.keycloak;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.StringReader;
import java.net.URI;

public class Keycloak {

    private final static URI usersUri = UriBuilder.fromUri("https://keycloak.kheops.online").path("/auth/admin/realms/StaticLoginConnect/users").build();
    private KeycloakToken token;

    public Keycloak() {
        token = new KeycloakToken();
    }

    public KeycloakUser getUser(String userName) {

        Response response2 = ClientBuilder.newClient().target(usersUri).request().header(HttpHeaders.AUTHORIZATION, "Bearer "+token.getToken()).get();
        String output2 = response2.readEntity(String.class);
        JsonReader jsonReader2 = Json.createReader(new StringReader(output2));
        JsonArray reply2 = jsonReader2.readArray();
        return new KeycloakUser(reply2);
    }



}


