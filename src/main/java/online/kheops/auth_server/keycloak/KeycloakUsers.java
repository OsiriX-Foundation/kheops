package online.kheops.auth_server.keycloak;

import online.kheops.auth_server.user.UserNotFoundException;

import javax.json.JsonArray;

public class KeycloakUsers {

    private JsonArray users;

    public KeycloakUsers(JsonArray users) {
        this.users = users;
    }

    public JsonArray getUsers() {
        return users;
    }

    public String getEmail(int index) {
        return users.getJsonObject(index).getString("email");
    }

    public String getId(int index) {
        return users.getJsonObject(index).getString("id");
    }

    public int verifyEmail(String email) throws UserNotFoundException {
        for (int index = 0; index < users.size(); index++) {
            if (users.getJsonObject(index).getString("email").compareTo(email) == 0) {
                return index;
            }
        }
        throw new UserNotFoundException();
    }

    public int size () {
        return users.size();
    }


}
