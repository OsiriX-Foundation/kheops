package online.kheops.auth_server.report_provider.metadata.parameters;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;

public enum ListUriParameter implements ListParameter<URI>
{
    REDIRECT_URIS("redirect_uris"),
    REQUEST_URIS("request_uris"),
    POST_LOGOUT_REDIRECT_URIS("post_logout_redirect_uris");

    final private String key;

    ListUriParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }


    @Override
    public URI innerValueFrom(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            try {
                return new URI(((JsonString) jsonValue).getString());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("bad URI", e);
            }
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }

    @Override
    public JsonValue jsonFromInnerValue(URI value) {
        return Json.createValue(value.toString());
    }
}
