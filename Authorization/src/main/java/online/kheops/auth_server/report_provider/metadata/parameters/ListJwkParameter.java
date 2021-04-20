package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Jwk;

import javax.json.JsonObject;
import javax.json.JsonValue;

public enum ListJwkParameter implements ListParameter<Jwk> {
    JWKS("jwks");

    private final String key;

    ListJwkParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }


    @Override
    public Jwk innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonObject) {
            return Jwk.fromJson((JsonObject) jsonValue);
        } else {
            throw new IllegalArgumentException("json value is not an object");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(Jwk value) {
        return value.toJson();
    }
}
