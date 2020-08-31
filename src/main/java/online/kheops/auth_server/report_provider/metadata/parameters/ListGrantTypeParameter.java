package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.GrantType;
import online.kheops.auth_server.report_provider.NoKeyException;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum ListGrantTypeParameter implements ListParameter<GrantType> {
    GRANT_TYPES("grantTypes"),
    GRANT_TYPES_SUPPORTED("grant_types_supported");

    private final String key;

    ListGrantTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public GrantType innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            return GrantType.fromKey(((JsonString) jsonValue).getString());
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(GrantType value) {
        try {
            return Json.createValue(value.getKey());
        } catch (NoKeyException e) {
            throw new IllegalArgumentException("GrantType has no key with this value", e);
        }
    }
}
