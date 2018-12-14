package online.kheops.auth_server.album;

import online.kheops.auth_server.user.UsersPermission;

import javax.ws.rs.core.MultivaluedMap;

import static online.kheops.auth_server.util.Consts.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")

public final class AlbumParams {

    private final String name;
    private final String description;
    private final UsersPermission usersPermission;


    public AlbumParams(MultivaluedMap<String, String> formParameters) {

        name = extractName(formParameters);
        description = extractDescription(formParameters);
        usersPermission = extractUsersPermision(formParameters);

    }

    private String extractName(MultivaluedMap<String, String> formParameters) {

        if (formParameters.containsKey(NAME)) {
            return formParameters.get(NAME).get(0);
        } else {
            return"Album_name";
        }
    }
    private String extractDescription(MultivaluedMap<String, String> formParameters) {

        if (formParameters.containsKey("description")) {
            return formParameters.get("description").get(0);
        } else {
            return"";
        }
    }

    private UsersPermission extractUsersPermision(MultivaluedMap<String, String> formParameters) {
        final UsersPermission usersPermission = new UsersPermission();
        if (formParameters.containsKey("addUser")) { usersPermission.setAddUser(Boolean.valueOf(formParameters.get("addUser").get(0))); }
        if (formParameters.containsKey("downloadSeries")) { usersPermission.setAddUser(Boolean.valueOf(formParameters.get("downloadSeries").get(0))); }
        if (formParameters.containsKey("sendSeries")) { usersPermission.setAddUser(Boolean.valueOf(formParameters.get("sendSeries").get(0))); }
        if (formParameters.containsKey("deleteSeries")) { usersPermission.setAddUser(Boolean.valueOf(formParameters.get("deleteSeries").get(0))); }
        if (formParameters.containsKey("addSeries")) { usersPermission.setAddUser(Boolean.valueOf(formParameters.get("addSeries").get(0))); }
        if (formParameters.containsKey("writeComments")) { usersPermission.setAddUser(Boolean.valueOf(formParameters.get("writeComments").get(0))); }

        return usersPermission;
    }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public UsersPermission getUsersPermission() { return usersPermission; }
}