package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.token.TokenClientAuthenticationType;

public enum OptionalAuthMethodParameter implements OptionalParameter<TokenClientAuthenticationType> {
    TOKEN_ENDPOINT_AUTH_METHOD("token_endpoint_auth_method");

    private final String key;

    OptionalAuthMethodParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
