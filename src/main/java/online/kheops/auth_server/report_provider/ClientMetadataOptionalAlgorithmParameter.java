package online.kheops.auth_server.report_provider;

public enum ClientMetadataOptionalAlgorithmParameter implements ClientMetadataOptionalParameter<Algorithm> {
    ID_TOKEN_SIGNED_RESPONSE_ALG("id_token_signed_response_alg"),
    ID_TOKEN_ENCRYPTED_RESPONSE_ALG("id_token_encrypted_reponse_alg"),
    USERINFO_SIGNED_RESPONSE_ALG("userinfo_signed_response_alg"),
    USERINFO_ENCRYPTED_RESPONSE_ALG("userinfo_encrypted_response_alg"),
    REQUEST_OBJECT_SIGNING_ALG("request_object_signing_alg"),
    REQUEST_OBJECT_ENCRYPTION_ALG("request_object_encryption_alg"),
    TOKEN_ENDPOINT_AUTH_SIGNING_ALG("token_endpoint_auth_signing_alg");

    private final String key;

    ClientMetadataOptionalAlgorithmParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
