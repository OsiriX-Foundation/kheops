package online.kheops.auth_server.accesstoken;

final class CapabilityAccessTokenBuilder implements AccessTokenBuilder {
    @Override
    public AccessToken build(String assertionToken) throws BadAccessTokenException {
        return CapabilityAccessToken.getBuilder().build(assertionToken);
    }
}
