package online.kheops.auth_server.util;

public abstract class HttpHeaders {
    private HttpHeaders() {}

    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    public static final String X_TOTAL_COUNT = "X-Total-Count";
    public static final String X_TOKEN_SOURCE = "X-Token-Source";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

}
