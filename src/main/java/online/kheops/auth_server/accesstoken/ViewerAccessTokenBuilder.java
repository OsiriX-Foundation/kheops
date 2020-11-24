package online.kheops.auth_server.accesstoken;

import online.kheops.auth_server.token.TokenAuthenticationContext;
import online.kheops.auth_server.util.JweAesKey;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.lang.JoseException;

final class ViewerAccessTokenBuilder implements AccessTokenBuilder {
    private final TokenAuthenticationContext tokenAuthenticationContext;

    ViewerAccessTokenBuilder(TokenAuthenticationContext tokenAuthenticationContext) {
        this.tokenAuthenticationContext = tokenAuthenticationContext;
    }

    @Override
    public AccessToken build(String assertionToken, boolean verifySignature) throws AccessTokenVerificationException {

        try {
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
                    KeyManagementAlgorithmIdentifiers.A128KW));
            jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT,
                    ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256));
            jwe.setKey(JweAesKey.getInstance().getKey());

            jwe.setCompactSerialization(assertionToken);

            return ViewerAccessToken.getBuilder(tokenAuthenticationContext).build(assertionToken, jwe.getPayload());
        } catch (JoseException e) {
            throw new AccessTokenVerificationException("Unable to decode JWT", e);
        }
    }
}
