package online.kheops.auth_server.report_provider;

public enum ClientMetadataOptionalApplicationTypeParameter implements ClientMetadataOptionalParameter<ApplicationType> {
    APPLICATION_TYPE("application_type");

    private final String key;

    ClientMetadataOptionalApplicationTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
