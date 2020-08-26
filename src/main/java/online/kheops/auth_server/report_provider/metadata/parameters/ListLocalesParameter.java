package online.kheops.auth_server.report_provider.metadata.parameters;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.IllformedLocaleException;
import java.util.Locale;

public enum ListLocalesParameter implements ListParameter<Locale> {
  CLAIMS_LOCALES_SUPPORTED("claims_locales_supported"),
  UI_LOCALES_SUPPORTED("ui_locales_supported");

  private final String key;

  ListLocalesParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public Locale innerValueFrom(JsonValue jsonValue) {
    if (jsonValue instanceof JsonString) {
      return Locale.forLanguageTag(((JsonString) jsonValue).getString());
    } else {
      throw new IllformedLocaleException("Not a string");
    }
  }

  @Override
  public JsonValue jsonFromInnerValue(Locale locale) {
    return Json.createValue(locale.toLanguageTag());
  }
}
