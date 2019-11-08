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

    public String getEmail(int index)  {
        if (users.getJsonObject(index).containsKey("email")) {
            return users.getJsonObject(index).getString("email");
        } else {
            return null;
        }
    }
    public String getLastName(int index) {
        if (users.getJsonObject(index).containsKey("lastName")) {
            return users.getJsonObject(index).getString("lastName");
        } else {
            return null;
        }
    }
    public String getFirstName(int index) {
        if (users.getJsonObject(index).containsKey("firstName")) {
            return users.getJsonObject(index).getString("firstName");
        } else {
            return null;
        }
    }

    public String getId(int index) {
        return users.getJsonObject(index).getString("id");
    }

    public int verifyEmail(String email) throws UserNotFoundException {
        for (int index = 0; index < users.size(); index++) {
            if (users.getJsonObject(index).getString("email").equals(email)) {
                return index;
            }
        }
        throw new UserNotFoundException();
    }

    public int size () {
        return users.size();
    }
}
