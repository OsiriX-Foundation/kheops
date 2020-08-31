package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.NoKeyException;
import online.kheops.auth_server.report_provider.ResponseType;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum ListResponseTypeParameter implements ListParameter<ResponseType> {
    RESPONSE_TYPES("response_types"),
    RESPONSE_TYPES_SUPPORTED("response_types_supported");

    private final String key;

    ListResponseTypeParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public ResponseType innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            return ResponseType.fromKey(((JsonString) jsonValue).getString());
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(ResponseType value) {
        try {
            return Json.createValue(value.getKey());
        } catch (NoKeyException e) {
            throw new IllegalArgumentException("ResponseType has no key with this value", e);
        }
    }
}
