package online.kheops.auth_server.accesstoken;

// register this class with the AccessTokenVerifier
// instances will be instantiated
// the AccessTokenVerifier knows how to inject a ServletContext in the constructor
interface AccessTokenBuilder {
    AccessToken build(String assertionToken, boolean verifySignature) throws AccessTokenVerificationException;
}
