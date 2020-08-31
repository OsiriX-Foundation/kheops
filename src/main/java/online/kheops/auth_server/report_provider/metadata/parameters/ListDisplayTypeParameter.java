package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.DisplayType;
import online.kheops.auth_server.report_provider.NoKeyException;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum ListDisplayTypeParameter implements ListParameter<DisplayType> {
  DISPLAY_VALUES_SUPPORTED("display_values_supported");

  private final String key;

  ListDisplayTypeParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public DisplayType innerValueFrom(JsonValue jsonValue) {
    if (jsonValue instanceof JsonString) {
      return DisplayType.fromKey(((JsonString) jsonValue).getString());
    } else {
      throw new IllegalArgumentException("Not a string");
    }
  }

  @Override
  public JsonValue jsonFromInnerValue(DisplayType value) {
    try {
      return Json.createValue(value.getKey());
    } catch (NoKeyException e) {
      throw new IllegalArgumentException("DisplayType has no key with this value", e);
    }
  }
}
