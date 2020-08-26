package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.Algorithm;
import online.kheops.auth_server.report_provider.NoKeyException;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;

public enum OptionalAlgorithmParameter implements OptionalParameter<Algorithm> {
    ID_TOKEN_SIGNED_RESPONSE_ALG("id_token_signed_response_alg"),
    ID_TOKEN_ENCRYPTED_RESPONSE_ALG("id_token_encrypted_reponse_alg"),
    USERINFO_SIGNED_RESPONSE_ALG("userinfo_signed_response_alg"),
    USERINFO_ENCRYPTED_RESPONSE_ALG("userinfo_encrypted_response_alg"),
    REQUEST_OBJECT_SIGNING_ALG("request_object_signing_alg"),
    REQUEST_OBJECT_ENCRYPTION_ALG("request_object_encryption_alg"),
    TOKEN_ENDPOINT_AUTH_SIGNING_ALG("token_endpoint_auth_signing_alg");

    private final String key;

    OptionalAlgorithmParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Algorithm innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            return Algorithm.fromKey(((JsonString) jsonValue).getString());
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(Algorithm value) {
        try {
            return Json.createValue(value.getKey());
        } catch (NoKeyException e) {
            throw new IllegalArgumentException("Algorithm has no key", e);
        }
    }
}
