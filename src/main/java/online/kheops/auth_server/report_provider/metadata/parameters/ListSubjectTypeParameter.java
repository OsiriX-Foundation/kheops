package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.SubjectType;

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
}
