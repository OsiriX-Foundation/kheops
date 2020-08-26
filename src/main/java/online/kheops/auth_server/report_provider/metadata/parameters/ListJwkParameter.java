package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Jwk;

public enum ListJwkParameter implements ListParameter<Jwk> {
    JWKS("jwks");

    private final String key;

    ListJwkParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
