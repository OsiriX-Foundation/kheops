package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.DisplayType;

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
}
