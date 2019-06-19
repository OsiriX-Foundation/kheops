package online.kheops.auth_server.util;

import online.kheops.auth_server.user.UsersPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class KheopsLogBuilder {

    public enum ActionType {LIST_ALBUMS, LIST_USERS, NEW_ALBUM, EDIT_ALBUM, ADD_USER, REMOVE_USER, ADD_ADMIN, REMOVE_ADMIN, ADD_FAVORITE, REMOVE_FAVORITE, DELETE_ALBUM, GET_ALBUM,
        SHARE_STUDY_WITH_USER, SHARE_SERIES_WITH_USER, SHARE_STUDY_WITH_ALBUM, REMOVE_SERIES}

    private Map<String,String> log;
    private static final Logger LOG = Logger.getLogger(KheopsLogBuilder.class.getName());


    public KheopsLogBuilder() {
        log = new HashMap<>();
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
    public KheopsLogBuilder fromAlbum(String albumId) {
        log.put("fromAlbum", albumId);
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
        usersPermission.getDownloadSeries().ifPresent(downloarSeries -> log.put("downloadSeries", downloarSeries.toString()));
        usersPermission.getSendSeries().ifPresent(sendSeries -> log.put("sendSeries", sendSeries.toString()));
        usersPermission.getWriteComments().ifPresent(writeComments -> log.put("writeComments", writeComments.toString()));
        return this;
    }

    public KheopsLogBuilder study(String studyUID) {
        log.put("studyUID", studyUID);
        return this;
    }
    public KheopsLogBuilder series(String seriesUID) {
        log.put("seriesUID", seriesUID);
        return this;
    }
    public KheopsLogBuilder fromInbox(Boolean fromInbox) {
        log.put("fromInbox", fromInbox.toString());
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
