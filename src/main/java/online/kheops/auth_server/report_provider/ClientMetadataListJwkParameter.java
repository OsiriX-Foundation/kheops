package online.kheops.auth_server.report_provider;

public enum ClientMetadataListJwkParameter implements ClientMetadataListParameter<Jwk> {
    JWKS("jwks");

    private final String key;

    ClientMetadataListJwkParameter(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
