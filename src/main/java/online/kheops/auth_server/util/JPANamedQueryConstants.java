package online.kheops.auth_server.util;

public class JPANamedQueryConstants {

    private JPANamedQueryConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ALBUM_ID = "albumId";
    public static final String ALBUM = "album";

    public static final String USER_ID = "userId";
    public static final String USER = "user";
    public static final String USER_EMAIL = "userEmail";
    public static final String SEARCH_NAME = "searchName";
    public static final String SEARCH_EMAIL = "searchEmail";

    public static final String SERIES = "series";
    public static final String SERIES_UID = "seriesUID";

    public static final String STUDY = "study";
    public static final String STUDY_UID = "studyUID";

    public static final String SECRET = "secret";
    public static final String CAPABILITY_ID = "capabilityId";
    public static final String CLIENT_ID = "clientId";
    public static final String DATE_TIME_NOW = "dateTimeNow";
    public static final String CAPABILITY_SCOPE_TYPE = "capabilityScopeType";

    public static final String WEBHOOK_ID = "webhookId";
    public static final String WEBHOOK_URL = "webhookUrl";
    public static final String WEBHOOK_TRIGGER_ID = "webhookTriggerId";

}
