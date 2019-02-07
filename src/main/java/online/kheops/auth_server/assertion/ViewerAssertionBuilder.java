package online.kheops.auth_server.assertion;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;

import java.security.Key;

final class ViewerAssertionBuilder implements AssertionBuilder {

    private final String authorizationSecret;

    ViewerAssertionBuilder(String authorizationSecret) {
        this.authorizationSecret = authorizationSecret;
    }

    @Override
    public Assertion build(String assertionToken) throws BadAssertionException {

        try {
            final Key key = new AesKey(authorizationSecret.substring(0, 16).getBytes());
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                    KeyManagementAlgorithmIdentifiers.A128KW));
            jwe.setContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                    ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256));
            jwe.setKey(key);

            jwe.setCompactSerialization(assertionToken);

            return ViewerAssertion.getBuilder().build(jwe.getPayload());
        } catch (JoseException e) {
            throw new BadAssertionException("Unable to decode JWT", e);
        }
    }
}
