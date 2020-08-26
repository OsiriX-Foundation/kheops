package online.kheops.auth_server.report_provider.metadata.parameters;

public enum OptionalStringParameter implements OptionalParameter<String> {
    CLIENT_NAME("client_name", true),
    CLIENT_SECRET("client_secret", false);

    private final String key;
    private final boolean localizable;

    OptionalStringParameter(final String key, final boolean localizable) {
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
