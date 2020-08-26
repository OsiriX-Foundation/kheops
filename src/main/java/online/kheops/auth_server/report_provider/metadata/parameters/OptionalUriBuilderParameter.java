package online.kheops.auth_server.report_provider.metadata.parameters;

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
}
