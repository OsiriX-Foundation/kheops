package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.metadata.Parameter;

import java.util.OptionalInt;

public enum OptionalIntParameter implements Parameter<OptionalInt> {
    DEFAULT_MAX_AGE("default_max_age");

    private final String key;

    OptionalIntParameter(final String key) {
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
