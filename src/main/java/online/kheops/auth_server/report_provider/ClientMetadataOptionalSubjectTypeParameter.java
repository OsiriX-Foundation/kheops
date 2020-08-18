package online.kheops.auth_server.report_provider;

public enum ClientMetadataOptionalSubjectTypeParameter implements ClientMetadataOptionalParameter<SubjectType> {
    SUBJECT_TYPE("subject_type");

    private final String key;

    ClientMetadataOptionalSubjectTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
