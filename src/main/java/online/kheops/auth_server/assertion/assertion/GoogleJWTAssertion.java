package online.kheops.auth_server.assertion.assertion;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import online.kheops.auth_server.assertion.Assertion;
import online.kheops.auth_server.assertion.exceptions.BadAssertionException;
import online.kheops.auth_server.assertion.exceptions.DownloadKeyException;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class GoogleJWTAssertion implements Assertion {
    private static final String GOOGLE_CONFIGURATION_URL = "https://accounts.google.com/.well-known/openid-configuration";

    private static class ConfigurationEntity {
        @XmlElement(name="jwks_uri")
        String jwksURI;
    }

    private String username;
    private String email;

    public void setAssertionToken(String assertionToken) throws BadAssertionException {

        final ConfigurationEntity configurationEntity;
        try {
            configurationEntity = ClientBuilder.newClient().target(GOOGLE_CONFIGURATION_URL).request(MediaType.APPLICATION_JSON).get(ConfigurationEntity.class);
        } catch (RuntimeException e) {
            throw new DownloadKeyException("Unable to download the google jwks_uri", e);
        }

        final URL googleCertsURL;
        try {
            googleCertsURL = new URL(configurationEntity.jwksURI);
        } catch (MalformedURLException e) {
            throw new DownloadKeyException("Google jwks_uri is not a valid URI", e);
        }

        final RSAKeyProvider keyProvider = new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String kid) {
                try {
                    return (RSAPublicKey) new UrlJwkProvider(googleCertsURL).get(kid).getPublicKey();
                } catch (JwkException e) {
                    throw new DownloadKeyException("Can't download the Google public RSA key", e);
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
                    .acceptExpiresAt(86400)
                    .withIssuer("accounts.google.com")
                    .build().verify(assertionToken);
        } catch (JWTVerificationException e) {
            throw new BadAssertionException("Verification of the Google token failed", e);
        }

        if (jwt.getSubject() == null) {
            throw new BadAssertionException("No subject present in the Google token");
        }
        Claim emailClaim = jwt.getClaim("email");
        if (emailClaim.isNull()) {
            throw new BadAssertionException("No email present in the Google token");
        }

        this.username = jwt.getSubject();
        this.email = emailClaim.asString();
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }
}
