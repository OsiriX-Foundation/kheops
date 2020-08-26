package online.kheops.auth_server.report_provider.metadata.parameters;

import java.net.URI;

public enum OptionalUriParameter implements OptionalParameter<URI> {
    LOGO_URI("logo_uri", true),
    CLIENT_URI("client_uri", true),
    POLICY_URI("policy_uri", true),
    TOS_URI("tos_uri", true),
    JWKS_URI("jwks_uri", false),
    SECTOR_IDENTIFIER_URI("sector_identifier_uri", false),
    USERINFO_ENDPOINT_URI("userinfo_endpoint", false),
    REGISTRATION_ENDPOINT("registration_endpoint", false),
    SERVICE_DOCUMENTATION("service_documentation", false),
    OP_POLICY_URI("op_policy_uri", false),
    OP_TOS_URI("op_tos_uri", false);

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
}
