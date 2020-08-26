package online.kheops.auth_server.report_provider.metadata.parameters;


import online.kheops.auth_server.report_provider.metadata.Parameter;

public enum StringParameter implements Parameter<String> {
    CLIENT_ID("client_id", false);

    private final String key;
    private final boolean localizable;

    StringParameter(final String key, final boolean localizable) {
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
