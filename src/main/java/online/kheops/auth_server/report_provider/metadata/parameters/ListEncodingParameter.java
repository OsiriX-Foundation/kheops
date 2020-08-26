package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Encoding;

public enum ListEncodingParameter implements ListParameter<Encoding> {
  ID_TOKEN_ENCRYPTION_ENC_VALUES_SUPPORTED("id_token_encryption_enc_values_supported"),
  USERINFO_ENCRYPTION_ENC_VALUES_SUPPORTED("userinfo_encryption_enc_values_supported"),
  REQUEST_OBJECT_ENCRYPTION_ENC_VALUES_SUPPORTED("request_object_encryption_enc_values_supported");

  private final String key;

  ListEncodingParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }
}
