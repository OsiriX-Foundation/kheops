package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.DisplayType;
import online.kheops.auth_server.report_provider.Encoding;
import online.kheops.auth_server.report_provider.NoKeyException;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

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

  @Override
  public Encoding innerValueFrom(JsonValue jsonValue) {
    if (jsonValue instanceof JsonString) {
      return Encoding.fromKey(((JsonString) jsonValue).getString());
    } else {
      throw new IllegalArgumentException("Not a string");
    }
  }

  @Override
  public JsonValue jsonFromInnerValue(Encoding value) {
    try {
      return Json.createValue(value.getKey());
    } catch (NoKeyException e) {
      throw new IllegalArgumentException("Encoding has no key with this value", e);
    }
  }

}
