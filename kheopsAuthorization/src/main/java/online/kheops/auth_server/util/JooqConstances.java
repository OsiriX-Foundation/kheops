package online.kheops.auth_server.util;

public class JooqConstances {

    private JooqConstances() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ALBUM_PK = "album_pk";
    public static final String ALBUM_ID = "album_id";
    public static final String ALBUM_NAME = "album_name";
    public static final String ALBUM_DESCRIPTION = "album_description";
    public static final String ALBUM_CREATED_TIME = "album_created_time";
    public static final String ALBUM_LAST_EVENT_TIME = "album_last_event_time";
    public static final String NUMBER_OF_USERS = "number_of_users";
    public static final String NUMBER_OF_COMMENTS = "number_of_comments";
    public static final String NUMBER_OF_STUDIES = "number_of_studies";
    public static final String NUMBER_OF_SERIES = "number_of_series";
    public static final String NUMBER_OF_INSTANCES = "number_of_instances";
    public static final String ADD_USER_PERMISSION = "add_user_permission";
    public static final String DOWNLOAD_USER_PERMISSION = "download_user_permission";
    public static final String SEND_SERIES_PERMISSION = "send_series_permission";
    public static final String DELETE_SERIES_PERMISION = "delete_series_permision";
    public static final String ADD_SERIES_PERMISSION = "add_series_permission";
    public static final String WRITE_COMMENT_PERMISSION = "write_comment_permission";
    public static final String FAVORITE = "favorite";
    public static final String NEW_COMMENT_NOTIFICATIONS = "new_comment_notifications";
    public static final String NEW_SERIES_NOTIFICATIONS = "new_series_notifications";
    public static final String ADMIN = "admin";
    public static final String MODALITIES = "modalities";
    public static final String COMMENT_EVENT_TYPE = "Comment";
}
