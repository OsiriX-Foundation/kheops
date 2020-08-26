package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.ApplicationType;

public enum OptionalApplicationTypeParameter implements OptionalParameter<ApplicationType> {
    APPLICATION_TYPE("application_type");

    private final String key;

    OptionalApplicationTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
