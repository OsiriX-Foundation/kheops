package online.kheops.auth_server.report_provider;

import java.net.URI;

public enum ClientMetadataURIParameter implements ClientMetadataParameter<URI> {
    INITIATE_LOGIN_URI("initiate_login_uri");

    private final String key;

    ClientMetadataURIParameter(final String key) {
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
}
