package online.kheops.auth_server.report_provider;

public enum ClientMetadataListResponseTypeParameter implements ClientMetadataListParameter<ResponseType> {
    RESPONSE_TYPES("response_types");

    private final String key;

    ClientMetadataListResponseTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
