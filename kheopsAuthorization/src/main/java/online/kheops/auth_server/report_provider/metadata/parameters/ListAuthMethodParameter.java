package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.token.TokenClientAuthenticationType;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum ListAuthMethodParameter implements ListParameter<TokenClientAuthenticationType> {
  TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED("token_endpoint_auth_methods_supported");

  private final String key;

  ListAuthMethodParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public TokenClientAuthenticationType innerValueFrom(JsonValue jsonValue) {
    if (jsonValue instanceof JsonString) {
      return TokenClientAuthenticationType.fromSchemeString(((JsonString) jsonValue).getString());
    } else {
      throw new IllegalArgumentException("Not a string");
    }
  }

  @Override
  public JsonValue jsonFromInnerValue(TokenClientAuthenticationType value) {
      return Json.createValue(value.getSchemeString());
  }
}
