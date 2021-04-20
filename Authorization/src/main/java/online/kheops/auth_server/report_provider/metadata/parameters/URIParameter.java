package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.metadata.Parameter;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;

public enum URIParameter implements Parameter<URI> {
    INITIATE_LOGIN_URI("initiate_login_uri");

    private final String key;

    URIParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public URI getEmptyValue() {
        throw new UnsupportedOperationException("Unable to get an empty value for a URI parameter");
    }

    @Override
    public URI valueFrom(JsonValue jsonValue) {
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
    public JsonValue jsonFrom(URI value) {
        return Json.createValue(value.toString());
    }
}
