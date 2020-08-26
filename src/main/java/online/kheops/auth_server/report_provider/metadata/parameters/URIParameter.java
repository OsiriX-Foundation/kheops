package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.metadata.Parameter;

import java.net.URI;

public enum URIParameter implements Parameter<URI> {
    INITIATE_LOGIN_URI("initiate_login_uri"),
    ISSUER("issuer"),
    AUTHORIZATION_ENDPOINT("authorization_endpoint");

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
}
