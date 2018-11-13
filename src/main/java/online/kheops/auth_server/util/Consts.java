package online.kheops.auth_server.util;

import javax.ws.rs.Priorities;

public class Consts {

    private Consts() {
        throw new IllegalStateException("Utility class");
    }

    public static final String StudyInstanceUID = "StudyInstanceUID";
    public static final String SeriesInstanceUID = "SeriesInstanceUID";

    public static final String QUERY_PARAMETER_LIMIT = "limit";
    public static final String QUERY_PARAMETER_OFFSET = "offset";
    public static final String QUERY_PARAMETER_FUZZY_MATCHING = "fuzzymatching";

    public static final long CAPABILITY_LEEWAY_SECONDE = 5;

    public static final int USER_ACCESS_PRIORITY = Priorities.USER + 1;
    public static final int ALBUM_ACCESS_PRIORITY = Priorities.USER + 2;
    public static final int ALBUM_PERMISSION_ACCESS_PRIORITY = Priorities.USER + 3;

}
