package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.metadata.Parameter;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonValue;
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

    @Override
    public OptionalInt valueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonNumber) {
            return OptionalInt.of(((JsonNumber) jsonValue).intValue());
        } else {
            throw new IllegalArgumentException("Not a number");
        }
    }

    @Override
    public JsonValue jsonFrom(OptionalInt value) {
        return Json.createValue(value.orElseThrow(() -> new IllegalArgumentException("Empty optional")));
    }
}
