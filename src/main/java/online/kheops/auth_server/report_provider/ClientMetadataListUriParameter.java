package online.kheops.auth_server.report_provider;

import java.net.URI;

public enum ClientMetadataListUriParameter implements ClientMetadataListParameter<URI>
{
    REDIRECT_URIS("redirect_uris"),
    REQUEST_URIS("request_uris"),
    POST_LOGOUT_REDIRECT_URIS("post_logout_redirect_uris");

    final private String key;

    ClientMetadataListUriParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
