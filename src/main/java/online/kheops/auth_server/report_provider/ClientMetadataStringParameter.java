package online.kheops.auth_server.report_provider;



public enum ClientMetadataStringParameter implements ClientMetadataParameter<String> {
    CLIENT_ID("client_id", false);

    private final String key;
    private final boolean localizable;

    ClientMetadataStringParameter(final String key, final boolean localizable) {
        this.key = key;
        this.localizable = localizable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getEmptyValue() {
        throw new UnsupportedOperationException("Unable to get an empty value for a String parameter");
    }

    @Override public boolean isLocalizable() {
        return localizable;
    }
}
