package online.kheops.auth_server.user;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import online.kheops.auth_server.keycloak.KeycloakContextListener;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;

public final class IdToken {

    private static final Client CLIENT = ClientBuilder.newClient();

    private final String name;
    private final String email;
    private final String sub;

    private static final String JWKS_CACHE_ALIAS = "jwksCache";
    private static final String PUBLIC_KEY_CACHE_ALIAS = "publicKeyCache";
    private static CacheManager jwksCacheManager = getJwksCacheManager();
    private static Cache<String, URL> jwksURLCache = jwksCacheManager.getCache(JWKS_CACHE_ALIAS, String.class, URL.class);
    private static Cache<String, RSAPublicKey> publicKeyCache = jwksCacheManager.getCache(PUBLIC_KEY_CACHE_ALIAS, String.class, RSAPublicKey.class);

    private static CacheManager getJwksCacheManager() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(JWKS_CACHE_ALIAS, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, URL.class, ResourcePoolsBuilder.heap(10))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))))
                .withCache(PUBLIC_KEY_CACHE_ALIAS, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, RSAPublicKey.class, ResourcePoolsBuilder.heap(10))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10))))
                .build();
        cacheManager.init();
        return cacheManager;
    }

    private static class ConfigurationEntity {
        @XmlElement(name="jwks_uri")
        String jwksURI;
    }

    public static final class Builder {
        private final String configurationUrl = KeycloakContextListener.getKeycloakOIDCConfigurationString();

        private final ServletContext servletContext;

        public Builder(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public IdToken build(String assertionToken) throws IdTokenVerificationException {
            URL jwksURL = getJwksURL(configurationUrl);

            final RSAKeyProvider keyProvider = new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String kid) {
                    return getRSAPublicKey(jwksURL, kid);
                }
                // implemented to get rid of warnings
                @Override public RSAPrivateKey getPrivateKey() {return null;}
                @Override public String getPrivateKeyId() {return null;}
            };

            final DecodedJWT jwt;
            try {
                jwt = JWT.require(Algorithm.RSA256(keyProvider))
                        .acceptLeeway(99999)
                        .withIssuer(getIssuer())
                        .acceptLeeway(99999)
                        .build()
                        .verify(assertionToken);
            } catch (JWTVerificationException e) {
                throw new IdTokenVerificationException("Verification of the token failed, configuration URL:" + configurationUrl, e);
            }

            if (jwt.getSubject() == null) {
                throw new IdTokenVerificationException("No subject present in the token, configuration URL:" + configurationUrl);
            }

            final String email;
            try {
                email = jwt.getClaim("email").asString();
            } catch (ClassCastException | JWTDecodeException e) {
                throw new IdTokenVerificationException("Unable to read the 'email'", e);
            }

            final String name;
            try {
                name = jwt.getClaim("name").asString();
            } catch (ClassCastException | JWTDecodeException e) {
                throw new IdTokenVerificationException("Unable to read the 'name'", e);
            }

            return new IdToken(jwt.getSubject(), email, name);
        }

        private static synchronized URL getJwksURL(String configurationUrl) {
            URL jwksURL = jwksURLCache.get(configurationUrl);

            if (jwksURL == null) {
                final ConfigurationEntity configurationEntity;
                try {
                    configurationEntity = CLIENT.target(configurationUrl).request(MediaType.APPLICATION_JSON).get(ConfigurationEntity.class);
                } catch (RuntimeException e) {
                    throw new DownloadKeyException("Unable to download the jwks_uri, configuration URL:" + configurationUrl, e);
                }

                try {
                    jwksURL = new URL(configurationEntity.jwksURI);
                } catch (MalformedURLException e) {
                    throw new DownloadKeyException("jwks_uri is not a valid URI, configuration URL:\" + configurationUrl", e);
                }
                jwksURLCache.put(configurationUrl, jwksURL);
            }
            return jwksURL;
        }

        private static synchronized RSAPublicKey getRSAPublicKey(URL jwksURL, String keyId) {
            RSAPublicKey publicKey = publicKeyCache.get(jwksURL.toString() + "_" + keyId);

            if (publicKey == null) {
                try {
                    publicKey = (RSAPublicKey) new UrlJwkProvider(jwksURL).get(keyId).getPublicKey();
                } catch (JwkException e) {
                    throw new DownloadKeyException("Can't download the public RSA key, jwks URL:" + jwksURL, e);
                }
                publicKeyCache.put(jwksURL.toString() + "_" + keyId, publicKey);
            }

            return publicKey;
        }

        private String getIssuer() {
            return servletContext.getInitParameter("online.kheops.keycloak.uri") + "/auth/realms/" +
                    servletContext.getInitParameter("online.kheops.keycloak.realms");
        }
    }

    private IdToken(String sub, String email, String name) {
        this.sub = sub;
        this.email = email;
        this.name = name;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSub() { return sub; }
}
