package online.kheops.auth_server.keycloak;

import javax.json.JsonArray;

public class KeycloakUser {

    private JsonArray users;

    public KeycloakUser(JsonArray users) {
        this.users = users;
    }

    public JsonArray getUsers() {
        return users;
    }
}
