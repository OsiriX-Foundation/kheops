package online.kheops.auth_server.report_provider.metadata.parameters;

import javax.json.JsonValue;

public enum OptionalBooleanParameter implements OptionalParameter<Boolean> {
    REQUIRE_AUTH_TIME("require_auth_time"),
    CLAIMS_PARAMETER_SUPPORTED("claims_parameter_supported"),
    REQUEST_PARAMETER_SUPPORTED("request_parameter_supported"),
    REQUEST_URI_PARAMETER_SUPPORTED("request_uri_parameter_supported"),
    REQUIRE_REQUEST_URI_REGISTRATION("require_request_uri_registration"),
    EMAIL_VERIFIED("email_verified"),
    PHONE_NUMBER_VERIFIED("phone_number_verified");

    private final String key;

    OptionalBooleanParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Boolean innerValueFrom(JsonValue jsonValue) {
        if (jsonValue.equals(JsonValue.TRUE)) {
            return true;
        } else if (jsonValue.equals(JsonValue.FALSE)) {
            return false;
        } else {
            throw new IllegalArgumentException("Not a boolean value");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(Boolean value) {
        return null;
    }
}
