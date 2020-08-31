package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Algorithm;
import online.kheops.auth_server.report_provider.ApplicationType;
import online.kheops.auth_server.report_provider.NoKeyException;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum OptionalApplicationTypeParameter implements OptionalParameter<ApplicationType> {
    APPLICATION_TYPE("application_type");

    private final String key;

    OptionalApplicationTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public ApplicationType innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            return ApplicationType.fromKey(((JsonString) jsonValue).getString());
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(ApplicationType value) {
        try {
            return Json.createValue(value.getKey());
        } catch (NoKeyException e) {
            throw new IllegalArgumentException("ApplicationType has no key with this value", e);
        }
    }
}
