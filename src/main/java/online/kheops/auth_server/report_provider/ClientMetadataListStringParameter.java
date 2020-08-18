package online.kheops.auth_server.report_provider;

public enum ClientMetadataListStringParameter implements ClientMetadataListParameter<String> {
    CONTACTS("contacts"),
    DEFAULT_ACR_VALUES("default_acr_values");

    private final String key;

    ClientMetadataListStringParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
