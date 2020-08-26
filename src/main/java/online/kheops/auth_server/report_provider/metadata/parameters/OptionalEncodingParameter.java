package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Encoding;

public enum OptionalEncodingParameter implements OptionalParameter<Encoding> {
    ID_TOKEN_ENCRYPTED_RESPONSE_ENC("id_token_encrypted_repsonse_enc"),
    USERINFO_ENCRYPTED_RESPONSE_ENC("userinfo_encrypted_response_enc"),
    REQUEST_OBJECT_ENCRYPTION_ENC("request_object_encryption_enc");

    private final String key;

    OptionalEncodingParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
