package online.kheops.auth_server.util;

import online.kheops.auth_server.accesstoken.AccessToken;
import online.kheops.auth_server.user.UsersPermission;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class KheopsLogBuilder {

    public enum ActionType {LIST_ALBUMS, LIST_USERS, NEW_ALBUM, EDIT_ALBUM, ADD_USER, REMOVE_USER, ADD_ADMIN, REMOVE_ADMIN, ALBUM_ADD_FAVORITE, ALBUM_REMOVE_FAVORITE, DELETE_ALBUM, GET_ALBUM,
        SHARE_STUDY_WITH_USER, SHARE_SERIES_WITH_USER, SHARE_STUDY_WITH_ALBUM, SHARE_SERIES_WITH_ALBUM, REMOVE_SERIES, REMOVE_STUDY, APPROPRIATE_STUDY, APPROPRIATE_SERIES,
        NEW_CAPABILITY, REVOKE_CAPABILITY, GET_CAPABILITY, GET_CAPABILITIES, INTROSPECT_CAPABILITY,
        ADD_FAVORITE_SERIES, ADD_FAVORITE_STUDY, REMOVE_FAVORITE_SERIES, REMOVE_FAVORITE_STUDY,
        QIDO_STUDIES, QIDO_STUDY, QIDO_STUDY_METADATA,
        POST_COMMENT, LIST_EVENTS,
        FETCH, TEST_USER, USER_INFO,
        NEW_REPORT_PROVIDER, NEW_REPORT, REPORT_PROVIDER_CONFIGURATION, LIST_REPORT_PROVIDERS, GET_REPORT_PROVIDER, DELETE_REPORT_PROVIDER, EDIT_REPORT_PROVIDER, REPORT_PROVIDER_METADATA,
        NEW_TOKEN, INTROSPECT_TOKEN}

    private ArrayList<LogEntry> log;
    private static final Logger LOG = Logger.getLogger(KheopsLogBuilder.class.getName());


    public KheopsLogBuilder() {
        log = new ArrayList<>();
    }

    public KheopsLogBuilder tokenType(AccessToken.TokenType tokenType) {
        log.add(new LogEntry("tokenType", tokenType.name()));
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
        StringBuilder logString = new StringBuilder();
        for (LogEntry pair:log) {
            logString.append(pair.getKey()).append("=").append(pair.getValue()).append(" ");
        }
        LOG.log(KheopsLevel.KHEOPS, logString.toString());
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
