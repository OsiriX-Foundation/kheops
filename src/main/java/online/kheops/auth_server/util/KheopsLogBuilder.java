package online.kheops.auth_server.util;

import online.kheops.auth_server.user.UsersPermission;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class KheopsLogBuilder {

    public enum ActionType {LIST_ALBUMS, LIST_USERS, NEW_ALBUM, EDIT_ALBUM, ADD_USER, REMOVE_USER, ADD_ADMIN, REMOVE_ADMIN, ADD_FAVORITE, REMOVE_FAVORITE, DELETE_ALBUM, GET_ALBUM,
        SHARE_STUDY_WITH_USER, SHARE_SERIES_WITH_USER, SHARE_STUDY_WITH_ALBUM, SHARE_SERIES_WITH_ALBUM, REMOVE_SERIES, REMOVE_STUDY, APPROPRIATE_STUDY, APPROPRIATE_SERIES}

    public enum PrincipalType {CAPABILITY, USER, VIEWER, REPORT_PROVIDER}

    private ArrayList<LogEntry> log;
    private static final Logger LOG = Logger.getLogger(KheopsLogBuilder.class.getName());


    public KheopsLogBuilder() {
        log = new ArrayList<>();
    }

    public KheopsLogBuilder principalType(PrincipalType principalType) {
        log.add(new LogEntry("type", principalType.name()));
        return this;
    }

    public KheopsLogBuilder scope(String scope) {
        log.add(new LogEntry("scope", scope));
        return this;
    }

    public KheopsLogBuilder clientID(String clientID) {
        log.add(new LogEntry("clientID", clientID));
        return this;
    }

    public KheopsLogBuilder capabilityID(String capabilityID) {
        log.add(new LogEntry("capabilityID", capabilityID));
        return this;
    }

    public KheopsLogBuilder user(String userId) {
        log.add(new LogEntry("user", userId));
        return this;
    }
    public KheopsLogBuilder targetUser(String userId) {
        log.add(new LogEntry("target_user", userId));
        return this;
    }
    public KheopsLogBuilder album(String albumId) {
        log.add(new LogEntry("album", albumId));
        return this;
    }
    public KheopsLogBuilder fromAlbum(String albumId) {
        log.add(new LogEntry("fromAlbum", albumId));
        return this;
    }
    public KheopsLogBuilder action(ActionType action) {
        log.add(new LogEntry("action", action.name()));
        return this;
    }
    public KheopsLogBuilder userPermission(UsersPermission usersPermission) {
        usersPermission.getAddSeries().ifPresent(addSeries -> log.add(new LogEntry("addSeries", addSeries.toString())));
        usersPermission.getAddUser().ifPresent(addUser -> log.add(new LogEntry("addUser", addUser.toString())));
        usersPermission.getDeleteSeries().ifPresent(deleteSeries -> log.add(new LogEntry("deleteSeries", deleteSeries.toString())));
        usersPermission.getDownloadSeries().ifPresent(downloarSeries -> log.add(new LogEntry("downloadSeries", downloarSeries.toString())));
        usersPermission.getSendSeries().ifPresent(sendSeries -> log.add(new LogEntry("sendSeries", sendSeries.toString())));
        usersPermission.getWriteComments().ifPresent(writeComments -> log.add(new LogEntry("writeComments", writeComments.toString())));
        return this;
    }

    public KheopsLogBuilder study(String studyUID) {
        log.add(new LogEntry("studyUID", studyUID));
        return this;
    }
    public KheopsLogBuilder series(String seriesUID) {
        log.add(new LogEntry("seriesUID", seriesUID));
        return this;
    }


    public void log() {
        String logString = "";
        for (LogEntry pair:log) {
            logString += pair.getKey() + "=" + pair.getValue() + " ";
        }
        LOG.log(KheopsLevel.KHEOPS, logString);
    }





    private final class LogEntry implements Map.Entry<String, String> {
        private final String key;
        private String value;

        public LogEntry(String key, String value) {
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
