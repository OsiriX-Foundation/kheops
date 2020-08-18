package online.kheops.auth_server.report_provider;

public enum ClientMetadataListGrantTypeParameter implements ClientMetadataListParameter<GrantType> {
    GRANT_TYPES("grantTypes");

    private final String key;

    ClientMetadataListGrantTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
