package online.kheops.auth_server.report_provider.metadata.parameters;

import javax.json.Json;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;

public enum OptionalUriParameter implements OptionalParameter<URI> {
    ISSUER("issuer", false),
    TOKEN_ENDPOINT("token_endpoint", false),
    AUTHORIZATION_ENDPOINT("authorization_endpoint", false),
    LOGO_URI("logo_uri", true),
    CLIENT_URI("client_uri", true),
    POLICY_URI("policy_uri", true),
    TOS_URI("tos_uri", true),
    JWKS_URI("jwks_uri", false),
    SECTOR_IDENTIFIER_URI("sector_identifier_uri", false),
    USERINFO_ENDPOINT("userinfo_endpoint", false),
    REGISTRATION_ENDPOINT("registration_endpoint", false),
    SERVICE_DOCUMENTATION("service_documentation", false),
    OP_POLICY_URI("op_policy_uri", false),
    OP_TOS_URI("op_tos_uri", false),
    PROFILE("profile", false),
    PICTURE("picture", false),
    WEBSITE("website", false);

    private final String key;
    private final boolean localizable;

    OptionalUriParameter(final String key, final boolean localizable){
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
