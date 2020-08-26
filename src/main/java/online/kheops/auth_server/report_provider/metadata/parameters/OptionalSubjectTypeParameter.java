package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.SubjectType;

public enum OptionalSubjectTypeParameter implements OptionalParameter<SubjectType> {
    SUBJECT_TYPE("subject_type");

    private final String key;

    OptionalSubjectTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
