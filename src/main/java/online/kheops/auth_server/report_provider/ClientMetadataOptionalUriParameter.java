package online.kheops.auth_server.report_provider;

import java.net.URI;

public enum ClientMetadataOptionalUriParameter implements ClientMetadataOptionalParameter<URI> {
    LOGO_URI("logo_uri", true),
    CLIENT_URI("client_uri", true),
    POLICY_URI("policy_uri", true),
    TOS_URI("tos_uri", true),
    JWKS_URI("jwks_uri", false),
    SECTOR_IDENTIFIER_URI("sector_identifier_uri", false);

    private final String key;
    private final boolean localizable;

    ClientMetadataOptionalUriParameter(final String key, final boolean localizable){
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
