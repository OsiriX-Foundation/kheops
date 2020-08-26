package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Algorithm;

public enum ListAlgorithmParameter implements ListParameter<Algorithm> {
  ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED("id_token_signing_alg_values_supported"),
  ID_TOKEN_ENCRYPTION_ALG_VALUES_SUPPORTED("id_token_encryption_alg_values_supported"),
  USERINFO_SIGNING_ALG_VALUES_SUPPORTED("userinfo_signing_alg_values_supported"),
  USERINFO_ENCRYPTION_ALG_VALUES_SUPPORTED("userinfo_encryption_alg_values_supported"),
  REQUEST_OBJECT_SIGNING_ALG_VALUES_SUPPORTED("request_object_signing_alg_values_supported"),
  REQUEST_OBJECT_ENCRYPTION_ALG_VALUES_SUPPORTED("request_object_encryption_alg_values_supported"),
  TOKEN_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED("token_endpoint_auth_signing_alg_values_supported");

  private final String key;

  ListAlgorithmParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }
}
