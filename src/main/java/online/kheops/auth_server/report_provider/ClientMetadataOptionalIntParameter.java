package online.kheops.auth_server.report_provider;

import java.util.OptionalInt;

public enum ClientMetadataOptionalIntParameter implements ClientMetadataParameter<OptionalInt> {
    DEFAULT_MAX_AGE("default_max_age");

    private final String key;

    ClientMetadataOptionalIntParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public OptionalInt getEmptyValue() {
        return OptionalInt.empty();
    }
}
