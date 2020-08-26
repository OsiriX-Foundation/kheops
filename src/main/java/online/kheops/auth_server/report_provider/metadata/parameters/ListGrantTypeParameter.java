package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.GrantType;

public enum ListGrantTypeParameter implements ListParameter<GrantType> {
    GRANT_TYPES("grantTypes"),
    GRANT_TYPES_SUPPORTED("grant_types_supported");

    private final String key;

    ListGrantTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
