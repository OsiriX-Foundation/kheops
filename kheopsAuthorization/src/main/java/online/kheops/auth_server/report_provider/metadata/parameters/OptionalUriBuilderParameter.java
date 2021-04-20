package online.kheops.auth_server.report_provider.metadata.parameters;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.ws.rs.core.UriBuilder;

public enum OptionalUriBuilderParameter implements OptionalParameter<UriBuilder> {
    KHEOPS_TARGET_LINK_URI_TEMPLATE("kheops_target_link_uri_template");

    private final String key;

    OptionalUriBuilderParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }


    @Override
    public UriBuilder innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            return UriBuilder.fromUri(((JsonString) jsonValue).getString());
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(UriBuilder value) {
        return Json.createValue(value.toTemplate());
    }
}
