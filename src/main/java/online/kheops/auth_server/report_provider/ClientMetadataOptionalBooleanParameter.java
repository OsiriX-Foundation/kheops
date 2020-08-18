package online.kheops.auth_server.report_provider;

public enum ClientMetadataOptionalBooleanParameter implements ClientMetadataOptionalParameter<Boolean> {
    REQUIRE_AUTH_TIME("require_auth_time");

    private final String key;

    ClientMetadataOptionalBooleanParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
