package online.kheops.auth_server.report_provider.metadata.parameters;

public enum ListStringParameter implements ListParameter<String> {
    CONTACTS("contacts"),
    DEFAULT_ACR_VALUES("default_acr_values"),
    SCOPES_SUPPORTED("scopes_supported"),
    ACR_VALUES_SUPPORTED("acr_values_supported"),
    CLAIMS_SUPPORTED("claims_supported");

    private final String key;

    ListStringParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
