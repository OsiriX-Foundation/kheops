package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.ClaimType;
import online.kheops.auth_server.report_provider.GrantType;
import online.kheops.auth_server.report_provider.NoKeyException;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum ListClaimTypeParameter implements ListParameter<ClaimType> {
  CLAIM_TYPES_SUPPORTED("claim_types_supported");

  private final String key;

  ListClaimTypeParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public ClaimType innerValueFrom(JsonValue jsonValue) {
    if (jsonValue instanceof JsonString) {
      return ClaimType.fromKey(((JsonString) jsonValue).getString());
    } else {
      throw new IllegalArgumentException("Not a string");
    }
  }

  @Override
  public JsonValue jsonFromInnerValue(ClaimType value) {
    try {
      return Json.createValue(value.getKey());
    } catch (NoKeyException e) {
      throw new IllegalArgumentException("ClaimType has no key", e);
    }
  }
}
