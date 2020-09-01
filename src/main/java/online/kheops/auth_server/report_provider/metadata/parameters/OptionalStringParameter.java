package online.kheops.auth_server.report_provider.metadata.parameters;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum OptionalStringParameter implements OptionalParameter<String> {
    CLIENT_NAME("client_name", true),
    CLIENT_SECRET("client_secret", false),
    SUB("sub", false),
    NAME("name", false),
    GIVEN_NAME("given_name", false),
    FAMILY_NAME("family_name", false),
    MIDDLE_NAME("middle_name", false),
    NICKNAME("nickname", false),
    PREFERRED_USERNAME("preferred_username", false),
    EMAIL("email", false),
    GENDER("gender", false);

    private final String key;
    private final boolean localizable;

    OptionalStringParameter(final String key, final boolean localizable) {
        this.key = key;
        this.localizable = localizable;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean isLocalizable() {
        return localizable;
    }

    @Override
    public String innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            return ((JsonString) jsonValue).getString();
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(String value) {
        return Json.createValue(value);
    }
}
