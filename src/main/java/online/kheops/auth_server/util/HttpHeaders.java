package online.kheops.auth_server.util;

public abstract class HttpHeaders {
    private HttpHeaders() {}

    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    public static final String X_TOTAL_COUNT = "X-Total-Count";
    public static final String X_AUTHORIZATION_SOURCE = "X-Authorization-Source";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_LINK_AUTHORIZATION = "X-Link-Authorization";
    public static final String X_KHEOPS_SIGNATURE = "X-Kheops-Signature";
    public static final String X_KHEOPS_DELIVERY = "X-Kheops-Delivery";
    public static final String X_KHEOPS_ATTEMPT = "X-Kheops-Attempt";
    public static final String X_KHEOPS_EVENT = "X-Kheops-Event";

}
