package online.kheops.auth_server.util;

import javax.ws.rs.Priorities;
import java.time.Duration;

public class Consts {

    private Consts() {
        throw new IllegalStateException("Utility class");
    }

    public static final String StudyInstanceUID = "StudyInstanceUID";
    public static final String SeriesInstanceUID = "SeriesInstanceUID";

    public static final String QUERY_PARAMETER_LIMIT = "limit";
    public static final String QUERY_PARAMETER_OFFSET = "offset";
    public static final String QUERY_PARAMETER_FUZZY_MATCHING = "fuzzymatching";
    public static final String QUERY_PARAMETER_SORT = "sort";

    public static final String ALBUM = "album";
    public static final String INBOX = "inbox";

    public static class USER_IN_ROLE {
        public static final String CAPABILITY = "capability";
        public static final String VIEWER_TOKEN = "viewerToken";
    }


    //AlbumQueryParams.java
    public static final String FAVORITE = "favorite";
    public static final String CUSTOM_DICOM_TAG_FAVORITE = "00012345";
    public static final String COMMENTS = "comments";
    public static final String CUSTOM_DICOM_TAG_COMMENTS = "00012346";
    public static final String NAME = "name";
    public static final String CREATED_TIME = "created_time";
    public static final String LAST_EVENT_TIME = "last_event_time";

    public static final long CAPABILITY_LEEWAY_SECOND = 5;

    public static final int USER_ACCESS_PRIORITY = Priorities.USER + 1;
    public static final int ALBUM_ACCESS_PRIORITY = Priorities.USER + 2;
    public static final int ALBUM_PERMISSION_ACCESS_PRIORITY = Priorities.USER + 3;
    public static final int UID_VALIDATOR_PRIORITY = Priorities.USER - 1;
    public static final int VIEWER_TOKEN_ACCESS_PRIORITY = Priorities.USER - 2;

    //Cache
    public static class CACHE_USER {
        public static final Duration DURATION = Duration.ofMinutes(20);
        public static final int SIZE = 100;
    }


}
