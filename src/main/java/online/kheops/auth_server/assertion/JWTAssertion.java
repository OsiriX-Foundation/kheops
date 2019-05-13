package online.kheops.auth_server.assertion;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

final class JWTAssertion implements Assertion {
    private static final Client CLIENT = ClientBuilder.newClient();

    private static class ConfigurationEntity {
        @XmlElement(name="jwks_uri")
        String jwksURI;
    }

    private final String email;
    private final String sub;


    static final class Builder {
        private final String configurationUrl;

        Builder(String configurationUrl) {
            this.configurationUrl = configurationUrl;
        }

        JWTAssertion build(String assertionToken) throws BadAssertionException {
            final ConfigurationEntity configurationEntity;
            try {
                configurationEntity = CLIENT.target(configurationUrl).request(MediaType.APPLICATION_JSON).get(ConfigurationEntity.class);
            } catch (RuntimeException e) {
                throw new DownloadKeyException("Unable to download the jwks_uri, configuration URL:" + configurationUrl, e);
            }

            final URL googleCertsURL;
            try {
                googleCertsURL = new URL(configurationEntity.jwksURI);
            } catch (MalformedURLException e) {
                throw new DownloadKeyException("jwks_uri is not a valid URI, configuration URL:\" + configurationUrl", e);
            }

            final RSAKeyProvider keyProvider = new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String kid) {
                    try {
                        return (RSAPublicKey) new UrlJwkProvider(googleCertsURL).get(kid).getPublicKey();
                    } catch (JwkException e) {
                        throw new DownloadKeyException("Can't download the public RSA key, configuration URL:" + configurationUrl, e);
                    }
                }
                // implemented to get rid of warnings
                @Override public RSAPrivateKey getPrivateKey() {return null;}
                @Override public String getPrivateKeyId() {return null;}
            };

            final DecodedJWT jwt;
            try {
                jwt = JWT.require(Algorithm.RSA256(keyProvider))
                        .acceptLeeway(120)
                        .build().verify(assertionToken);
            } catch (JWTVerificationException e) {
                throw new BadAssertionException("Verification of the token failed, configuration URL:" + configurationUrl, e);
            }

            if (jwt.getSubject() == null) {
                throw new BadAssertionException("No subject present in the token, configuration URL:" + configurationUrl);
            }
            final Claim emailClaim = jwt.getClaim("email");
            if (emailClaim.isNull()) {
                throw new BadAssertionException("No email present in the token, configuration URL:" + configurationUrl);
            }

            return new JWTAssertion(jwt.getSubject(), emailClaim.asString());
        }
    }

    static Builder getBuilder(String configurationUrl) {
        return new Builder(configurationUrl);
    }

    private JWTAssertion(String sub, String email) {
        this.sub = sub;
        this.email = email;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean hasCapabilityAccess() {
        return true;
    }

    @Override
    public String getSub() {
        return sub;
    }

    @Override
    public TokenType getTokenType() {
        return TokenType.KEYCLOAK_TOKEN;
    }
}
