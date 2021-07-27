package online.kheops.auth_server.util;

import online.kheops.auth_server.accesstoken.AccessToken;
import online.kheops.auth_server.token.TokenProvenance;
import online.kheops.auth_server.user.UsersPermission;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class KheopsLogBuilder {

    public enum ActionType {LIST_ALBUMS, LIST_USERS, NEW_ALBUM, EDIT_ALBUM, ADD_USER, REMOVE_USER, ADD_ADMIN, REMOVE_ADMIN, ALBUM_ADD_FAVORITE, ALBUM_REMOVE_FAVORITE, DELETE_ALBUM, GET_ALBUM,
        SHARE_STUDY_WITH_USER, SHARE_SERIES_WITH_USER, SHARE_STUDY_WITH_ALBUM, SHARE_SERIES_WITH_ALBUM, REMOVE_SERIES, REMOVE_STUDY, APPROPRIATE_STUDY, APPROPRIATE_SERIES,
        NEW_CAPABILITY, REVOKE_CAPABILITY, GET_CAPABILITY, GET_CAPABILITIES, INTROSPECT_CAPABILITY,
        ADD_FAVORITE_SERIES, ADD_FAVORITE_STUDY, REMOVE_FAVORITE_SERIES, REMOVE_FAVORITE_STUDY,
        QIDO_STUDIES, QIDO_SERIES, QIDO_STUDY_METADATA,
        POST_COMMENT, LIST_EVENTS,
        FETCH, TEST_USER, USER_INFO, USERS_LIST,
        NEW_REPORT_PROVIDER, NEW_REPORT, REPORT_PROVIDER_CONFIGURATION, LIST_REPORT_PROVIDERS, GET_REPORT_PROVIDER, DELETE_REPORT_PROVIDER, EDIT_REPORT_PROVIDER, REPORT_PROVIDER_METADATA,
        NEW_TOKEN, INTROSPECT_TOKEN,
        REFRESH_TOKEN_GRANT, AUTHORIZATION_CODE_GRANT, PASSWORD_GRANT, CLIENT_CREDENTIALS_GRANT, JWT_ASSERTION_GRANT, SAML_ASSERTION_GRANT, TOKEN_EXCHANGE_GRANT,
        NEW_USER, UPDATE_USER,
        INBOX_INFO,
        NEW_WEBHOOK, REMOVE_WEBHOOK, GET_WEBHOOK, EDIT_WEBHOOK, LIST_WEBHOOK, TRIGGER_WEBHOOK,
        VERIFY_INSTANCE}

    private ArrayList<LogEntry> logEntry;
    private static final Logger LOG = Logger.getLogger(KheopsLogBuilder.class.getName());


    public KheopsLogBuilder() {
        logEntry = new ArrayList<>();
    }

    public KheopsLogBuilder tokenType(AccessToken.TokenType tokenType) {
        logEntry.add(new LogEntry("tokenType", tokenType.name()));
        return this;
    }

    public KheopsLogBuilder scope(String scope) {
        logEntry.add(new LogEntry("scope", scope));
        return this;
    }
    public KheopsLogBuilder albumScope(String albumId) {
        logEntry.add(new LogEntry("albumScope", albumId));
        return this;
    }

    public KheopsLogBuilder clientID(String clientID) {
        logEntry.add(new LogEntry("clientID", clientID));
        return this;
    }

    public KheopsLogBuilder webhookID(String webhookID) {
        logEntry.add(new LogEntry("webhookID", webhookID));
        return this;
    }

    public KheopsLogBuilder capabilityID(String capabilityID) {
        logEntry.add(new LogEntry("capabilityID", capabilityID));
        return this;
    }

    public KheopsLogBuilder ip(String ip) {
        logEntry.add(new LogEntry("sourceIP", ip.split(", ")[0]));
        return this;
    }

    public KheopsLogBuilder user(String userId) {
        logEntry.add(new LogEntry("user", userId));
        return this;
    }

    public KheopsLogBuilder provenance(TokenProvenance tokenProvenance) {
        tokenProvenance.getActingParty().ifPresent(actingParty -> logEntry.add(new LogEntry("actingParty", actingParty)));
        tokenProvenance.getAuthorizedParty().ifPresent(authorizedParty -> logEntry.add(new LogEntry("authorizedParty", authorizedParty)));
        tokenProvenance.getCapabilityTokenId().ifPresent(capabilityTokenId -> logEntry.add(new LogEntry("authorizedCapabilityTokenId", capabilityTokenId)));
        return this;
    }

    public KheopsLogBuilder link(boolean link) {
        logEntry.add(new LogEntry("link", String.valueOf(link)));
        return this;
    }
    public KheopsLogBuilder targetUser(String userId) {
        logEntry.add(new LogEntry("target_user", userId));
        return this;
    }
    public KheopsLogBuilder album(String albumId) {
        logEntry.add(new LogEntry("album", albumId));
        return this;
    }
    public KheopsLogBuilder fromAlbum(String albumId) {
        logEntry.add(new LogEntry("fromAlbum", albumId));
        return this;
    }
    public KheopsLogBuilder action(ActionType action) {
        logEntry.add(new LogEntry("action", action.name()));
        return this;
    }
    public KheopsLogBuilder events(String events) {
        logEntry.add(new LogEntry("events", events));
        return this;
    }
    public KheopsLogBuilder userPermission(UsersPermission usersPermission) {
        usersPermission.getAddSeries().ifPresent(addSeries -> logEntry.add(new LogEntry("addSeries", addSeries.toString())));
        usersPermission.getAddUser().ifPresent(addUser -> logEntry.add(new LogEntry("addUser", addUser.toString())));
        usersPermission.getDeleteSeries().ifPresent(deleteSeries -> logEntry.add(new LogEntry("deleteSeries", deleteSeries.toString())));
        usersPermission.getDownloadSeries().ifPresent(downloarSeries -> logEntry.add(new LogEntry("downloadSeries", downloarSeries.toString())));
        usersPermission.getSendSeries().ifPresent(sendSeries -> logEntry.add(new LogEntry("sendSeries", sendSeries.toString())));
        usersPermission.getWriteComments().ifPresent(writeComments -> logEntry.add(new LogEntry("writeComments", writeComments.toString())));
        return this;
    }

    public KheopsLogBuilder study(String studyUID) {
        logEntry.add(new LogEntry("studyUID", studyUID));
        return this;
    }
    public KheopsLogBuilder series(String seriesUID) {
        logEntry.add(new LogEntry("seriesUID", seriesUID));
        return this;
    }
    public KheopsLogBuilder isValid(boolean isValid) {
        logEntry.add(new LogEntry("isValid", String.valueOf(isValid)));
        return this;
    }
    public KheopsLogBuilder reason(String reason) {
        logEntry.add(new LogEntry("reason", reason));
        return this;
    }

    public void log() {
        StringBuilder logString = new StringBuilder();
        for (LogEntry pair: logEntry) {
            logString.append(pair.getKey()).append("=").append(pair.getValue()).append(" ");
        }
        LOG.log(KheopsLevel.KHEOPS, logString::toString);
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
