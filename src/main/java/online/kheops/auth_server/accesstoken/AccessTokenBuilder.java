package online.kheops.auth_server.accesstoken;

interface AccessTokenBuilder {
    AccessToken build(String assertionToken) throws BadAccessTokenException;
}
