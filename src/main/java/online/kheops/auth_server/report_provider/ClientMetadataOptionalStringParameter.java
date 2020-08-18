package online.kheops.auth_server.report_provider;

public enum ClientMetadataOptionalStringParameter implements ClientMetadataOptionalParameter<String> {
    CLIENT_NAME("client_name", true),
    CLIENT_SECRET("client_secret", false);

    private final String key;
    private final boolean localizable;

    ClientMetadataOptionalStringParameter(final String key, final boolean localizable) {
        this.key = key;
        this.localizable = localizable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean isLocalizable() {
        return localizable;
    }
}
