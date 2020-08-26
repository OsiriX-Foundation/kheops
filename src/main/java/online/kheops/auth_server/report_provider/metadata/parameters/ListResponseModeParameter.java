package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.ResponseMode;

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
}
