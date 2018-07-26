package online.kheops.zipper.token;

public enum AccessTokenType {
    JWT_BEARER_TOKEN("urn:ietf:params:oauth:grant-type:jwt-bearer"),
    CAPABILITY_TOKEN("urn:x-kheops:params:oauth:grant-type:capability");

    private final String urn;

    AccessTokenType(String urn) {
        this.urn = urn;
    }

    public String getUrn() {
        return urn;
    }

    @Override
    public String toString() {
        return urn;
    }
}
