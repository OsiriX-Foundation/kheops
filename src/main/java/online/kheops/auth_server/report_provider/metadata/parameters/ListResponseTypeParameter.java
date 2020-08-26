package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.ResponseType;

public enum ListResponseTypeParameter implements ListParameter<ResponseType> {
    RESPONSE_TYPES("response_types"),
    RESPONSE_TYPES_SUPPORTED("response_types_supported");

    private final String key;

    ListResponseTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
