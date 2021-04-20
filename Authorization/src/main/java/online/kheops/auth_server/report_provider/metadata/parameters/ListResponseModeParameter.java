package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.GrantType;
import online.kheops.auth_server.report_provider.NoKeyException;
import online.kheops.auth_server.report_provider.ResponseMode;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum ListResponseModeParameter implements ListParameter<ResponseMode> {
  RESPONSE_MODES_SUPPORTED("response_modes_supported");

  private final String key;

  ListResponseModeParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public ResponseMode innerValueFrom(JsonValue jsonValue) {
    if (jsonValue instanceof JsonString) {
      return ResponseMode.fromKey(((JsonString) jsonValue).getString());
    } else {
      throw new IllegalArgumentException("Not a string");
    }
  }

  @Override
  public JsonValue jsonFromInnerValue(ResponseMode value) {
    try {
      return Json.createValue(value.getKey());
    } catch (NoKeyException e) {
      throw new IllegalArgumentException("ResponseMode has no key with this value", e);
    }
  }
}
