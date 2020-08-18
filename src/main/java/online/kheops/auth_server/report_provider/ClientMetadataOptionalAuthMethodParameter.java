package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.token.TokenClientAuthenticationType;

public enum ClientMetadataOptionalAuthMethodParameter implements ClientMetadataOptionalParameter<TokenClientAuthenticationType> {
    TOKEN_ENDPOINT_AUTH_METHOD("token_endpoint_auth_method");

    private final String key;

    ClientMetadataOptionalAuthMethodParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
