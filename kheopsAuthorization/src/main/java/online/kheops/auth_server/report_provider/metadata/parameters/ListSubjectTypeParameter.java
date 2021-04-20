package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Algorithm;
import online.kheops.auth_server.report_provider.NoKeyException;
import online.kheops.auth_server.report_provider.SubjectType;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum ListSubjectTypeParameter implements ListParameter<SubjectType> {
  SUBJECT_TYPES_SUPPORTED("subject_types_supported");

  private final String key;

  ListSubjectTypeParameter(final String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public SubjectType innerValueFrom(JsonValue jsonValue) {
    if (jsonValue instanceof JsonString) {
      return SubjectType.fromKey(((JsonString) jsonValue).getString());
    } else {
      throw new IllegalArgumentException("Not a string");
    }
  }

  @Override
  public JsonValue jsonFromInnerValue(SubjectType value) {
    try {
      return Json.createValue(value.getKey());
    } catch (NoKeyException e) {
      throw new IllegalArgumentException("SubjectType has no key", e);
    }
  }
}
