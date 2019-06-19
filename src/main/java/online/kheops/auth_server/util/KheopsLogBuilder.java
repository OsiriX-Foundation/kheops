package online.kheops.auth_server.util;

import online.kheops.auth_server.user.UsersPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class KheopsLogBuilder {

    public enum ActionType {LIST_ALBUMS, LIST_USERS, NEW_ALBUM, EDIT_ALBUM, ADD_USER, REMOVE_USER, ADD_ADMIN, REMOVE_ADMIN, ADD_FAVORITE, REMOVE_FAVORITE, DELETE_ALBUM, GET_ALBUM}

    private Map<String,String> log;
    private Logger LOG;

    public KheopsLogBuilder() {
        log = new HashMap<>();
    }

    public KheopsLogBuilder logger(Logger logger) {
        this.LOG = logger;
        return this;
    }

    public KheopsLogBuilder user(String userId) {
        log.put("user", userId);
        return this;
    }
    public KheopsLogBuilder targetUser(String userId) {
        log.put("target_user", userId);
        return this;
    }
    public KheopsLogBuilder album(String albumId) {
        log.put("album", albumId);
        return this;
    }
    public KheopsLogBuilder action(ActionType action) {
        log.put("action", action.name());
        return this;
    }
    public KheopsLogBuilder userPermission(UsersPermission usersPermission) {
        usersPermission.getAddSeries().ifPresent(addSeries -> log.put("addSeries", addSeries.toString()));
        usersPermission.getAddUser().ifPresent(addUser -> log.put("addUser", addUser.toString()));
        usersPermission.getDeleteSeries().ifPresent(deleteSeries -> log.put("deleteSeries", deleteSeries.toString()));
        usersPermission.getDownloadSeries().ifPresent(downloarSeries -> log.put("downloarSeries", downloarSeries.toString()));
        usersPermission.getSendSeries().ifPresent(sendSeries -> log.put("sendSeries", sendSeries.toString()));
        usersPermission.getWriteComments().ifPresent(writeComments -> log.put("writeComments", writeComments.toString()));
        return this;
    }



    public void log() {
        String logString = "";
        for (Map.Entry<String, String> entry:log.entrySet()) {
            logString += entry.getKey() + "=" + entry.getValue() + " ";
        }
        LOG.log(KheopsLevel.KHEOPS, logString);
    }


}
