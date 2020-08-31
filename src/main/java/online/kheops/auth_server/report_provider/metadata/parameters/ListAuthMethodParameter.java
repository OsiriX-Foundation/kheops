package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.AuthenticationMethod;

public enum ListAuthMethodParameter implements ListParameter<AuthenticationMethod> {
  TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED("token_endpoint_auth_methods_supported");

  private final String key;

  ListAuthMethodParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }
}
