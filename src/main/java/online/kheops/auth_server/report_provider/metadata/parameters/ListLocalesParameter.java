package online.kheops.auth_server.report_provider.metadata.parameters;

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
}
