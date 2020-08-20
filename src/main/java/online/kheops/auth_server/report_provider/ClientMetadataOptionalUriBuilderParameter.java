package online.kheops.auth_server.report_provider;

import javax.ws.rs.core.UriBuilder;

public enum ClientMetadataOptionalUriBuilderParameter implements ClientMetadataOptionalParameter<UriBuilder> {
    KHEOPS_TARGET_LINK_URI_TEMPLATE("kheops_target_link_uri_template");

    private final String key;

    ClientMetadataOptionalUriBuilderParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
