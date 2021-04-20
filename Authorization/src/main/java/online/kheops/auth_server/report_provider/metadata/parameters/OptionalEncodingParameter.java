package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Encoding;
import online.kheops.auth_server.report_provider.NoKeyException;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;

public enum OptionalEncodingParameter implements OptionalParameter<Encoding> {
    ID_TOKEN_ENCRYPTED_RESPONSE_ENC("id_token_encrypted_response_enc"),
    USERINFO_ENCRYPTED_RESPONSE_ENC("userinfo_encrypted_response_enc"),
    REQUEST_OBJECT_ENCRYPTION_ENC("request_object_encryption_enc");

    private final String key;

    OptionalEncodingParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Encoding innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            return Encoding.fromKey(((JsonString) jsonValue).getString());
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(Encoding value) {
        try {
            return Json.createValue(value.getKey());
        } catch (NoKeyException e) {
            throw new IllegalArgumentException("Encoding has no key with this value", e);
        }
    }
}
