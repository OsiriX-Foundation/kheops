package online.kheops.auth_server.util;

import online.kheops.auth_server.user.UsersPermission;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class KheopsLogBuilder {

    public enum ActionType {LIST_ALBUMS, LIST_USERS, NEW_ALBUM, EDIT_ALBUM, ADD_USER, REMOVE_USER, ADD_ADMIN, REMOVE_ADMIN, ADD_FAVORITE, REMOVE_FAVORITE, DELETE_ALBUM, GET_ALBUM,
        SHARE_STUDY_WITH_USER, SHARE_SERIES_WITH_USER, SHARE_STUDY_WITH_ALBUM, REMOVE_SERIES, REMOVE_STUDY}

    private ArrayList<MyEntry> log;
    private static final Logger LOG = Logger.getLogger(KheopsLogBuilder.class.getName());


    public KheopsLogBuilder() {
        log = new ArrayList<>();
    }

    public KheopsLogBuilder user(String userId) {
        log.add(new MyEntry("user", userId));
        return this;
    }
    public KheopsLogBuilder targetUser(String userId) {
        log.add(new MyEntry("target_user", userId));
        return this;
    }
    public KheopsLogBuilder album(String albumId) {
        log.add(new MyEntry("album", albumId));
        return this;
    }
    public KheopsLogBuilder fromAlbum(String albumId) {
        log.add(new MyEntry("fromAlbum", albumId));
        return this;
    }
    public KheopsLogBuilder action(ActionType action) {
        log.add(new MyEntry("action", action.name()));
        return this;
    }
    public KheopsLogBuilder userPermission(UsersPermission usersPermission) {
        usersPermission.getAddSeries().ifPresent(addSeries -> log.add(new MyEntry("addSeries", addSeries.toString())));
        usersPermission.getAddUser().ifPresent(addUser -> log.add(new MyEntry("addUser", addUser.toString())));
        usersPermission.getDeleteSeries().ifPresent(deleteSeries -> log.add(new MyEntry("deleteSeries", deleteSeries.toString())));
        usersPermission.getDownloadSeries().ifPresent(downloarSeries -> log.add(new MyEntry("downloadSeries", downloarSeries.toString())));
        usersPermission.getSendSeries().ifPresent(sendSeries -> log.add(new MyEntry("sendSeries", sendSeries.toString())));
        usersPermission.getWriteComments().ifPresent(writeComments -> log.add(new MyEntry("writeComments", writeComments.toString())));
        return this;
    }

    public KheopsLogBuilder study(String studyUID) {
        log.add(new MyEntry("studyUID", studyUID));
        return this;
    }
    public KheopsLogBuilder series(String seriesUID) {
        log.add(new MyEntry("seriesUID", seriesUID));
        return this;
    }
    public KheopsLogBuilder fromInbox(Boolean fromInbox) {
        log.add(new MyEntry("fromInbox", fromInbox.toString()));
        return this;
    }


    public void log() {
        String logString = "";
        for (MyEntry pair:log) {
            logString += pair.getKey() + "=" + pair.getValue() + " ";
        }
        LOG.log(KheopsLevel.KHEOPS, logString);
    }





    private final class MyEntry implements Map.Entry<String, String> {
        private final String key;
        private String value;

        public MyEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            String old = this.value;
            this.value = value;
            return old;
        }
    }
}
