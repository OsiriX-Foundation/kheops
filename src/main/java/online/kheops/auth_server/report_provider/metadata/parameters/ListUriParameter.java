package online.kheops.auth_server.report_provider.metadata.parameters;

import java.net.URI;

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
}
