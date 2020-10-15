package online.kheops.auth_server.accesstoken;

import online.kheops.auth_server.util.JweAesKey;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.lang.JoseException;

import javax.servlet.ServletContext;

final class ViewerAccessTokenBuilder implements AccessTokenBuilder {
    private final ServletContext servletContext;

    ViewerAccessTokenBuilder(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public AccessToken build(String assertionToken, boolean verifySignature) throws AccessTokenVerificationException {

        try {
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                    KeyManagementAlgorithmIdentifiers.A128KW));
            jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                    ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256));
            jwe.setKey(JweAesKey.getInstance().getKey());

            jwe.setCompactSerialization(assertionToken);

            return ViewerAccessToken.getBuilder(servletContext).build(assertionToken, jwe.getPayload());
        } catch (JoseException e) {
            throw new AccessTokenVerificationException("Unable to decode JWT", e);
        }
    }
}
