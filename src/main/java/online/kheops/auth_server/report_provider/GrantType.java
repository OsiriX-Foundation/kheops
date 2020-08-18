package online.kheops.auth_server.report_provider;

public enum GrantType {
    AUTHORIZATION_CODE("authorization_code"),
    IMPLICIT("implicit"),
    REFRESH_TOKEN("refresh_token");

    final String name;

    GrantType(final String name) {
        this.name = name;
    }
}
