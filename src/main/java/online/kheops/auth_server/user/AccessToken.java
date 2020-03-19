package online.kheops.auth_server.user;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import online.kheops.auth_server.OIDCProviderContextListener;
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

public final class AccessToken {

    private static final Client CLIENT = ClientBuilder.newClient();

    private final String issuer;

    private static final String CONFIGURATION_URLS_CACHE_ALIAS = "configurationURLsCache";
    private static final String PUBLIC_KEY_CACHE_ALIAS = "publicKeyCache";
    private static CacheManager jwksCacheManager = getJwksCacheManager();
    private static Cache<String, ConfigurationURLs> configurationURLsCache = jwksCacheManager.getCache(CONFIGURATION_URLS_CACHE_ALIAS, String.class, ConfigurationURLs.class);
    private static Cache<String, RSAPublicKey> publicKeyCache = jwksCacheManager.getCache(PUBLIC_KEY_CACHE_ALIAS, String.class, RSAPublicKey.class);

    private static CacheManager getJwksCacheManager() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache(CONFIGURATION_URLS_CACHE_ALIAS, CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, ConfigurationURLs.class, ResourcePoolsBuilder.heap(10))
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
        @XmlElement(name="issuer")
        String issuer;
    }

    private static class ConfigurationURLs {
        private URL jwksURI;

        private String issuer;

        public ConfigurationURLs(ConfigurationEntity configurationEntity) throws MalformedURLException {
            this.jwksURI = new URL(configurationEntity.jwksURI);
            this.issuer = configurationEntity.issuer;
        }
    }

    public static final class Builder {
        private final String configurationUrl = OIDCProviderContextListener.getOIDCConfigurationString();

        private final ServletContext servletContext;

        public Builder(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public AccessToken build(String assertionToken) throws IdTokenVerificationException {
            ConfigurationURLs configurationURLs = getJwksURL(configurationUrl);

            final RSAKeyProvider keyProvider = new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String kid) {
                    return getRSAPublicKey(configurationURLs.jwksURI, kid);
                }
                // implemented to get rid of warnings
                @Override public RSAPrivateKey getPrivateKey() {return null;}
                @Override public String getPrivateKeyId() {return null;}
            };

            final DecodedJWT jwt;
            try {
                jwt = JWT.require(Algorithm.RSA256(keyProvider))
                        .acceptLeeway(60)
                        .withIssuer(configurationURLs.issuer)
                        .acceptLeeway(60)
                        .build()
                        .verify(assertionToken);
            } catch (JWTVerificationException e) {
                throw new IdTokenVerificationException("Verification of the token failed, configuration URL:" + configurationUrl, e);
            }

            final boolean verifyScope = Boolean.parseBoolean(servletContext.getInitParameter("online.kheops.use.scope"));
            if (verifyScope) {
                final Claim scopeClaim = jwt.getClaim("scope");
                if (scopeClaim.isNull() || scopeClaim.asString() == null) {
                    throw new IdTokenVerificationException("Missing scope claim in token");
                } else {
                    if (!scopeClaim.asString().contains("kheops")) {
                        throw new IdTokenVerificationException("Missing scope 'kheops' in token");
                    }
                }
            }

            if (jwt.getSubject() == null) {
                throw new IdTokenVerificationException("No subject present in the token, configuration URL:" + configurationUrl);
            }
            if (jwt.getIssuer() == null) {
                throw new IdTokenVerificationException("No Issuer present in the token, configuration URL:" + configurationUrl);
            }

            return new AccessToken(jwt.getIssuer());
        }

        private static synchronized ConfigurationURLs getJwksURL(String configurationUrl) {
            ConfigurationURLs configurationURLs = configurationURLsCache.get(configurationUrl);

            if (configurationURLs == null) {
                final ConfigurationEntity configurationEntity;
                try {
                    configurationEntity = CLIENT.target(configurationUrl).request(MediaType.APPLICATION_JSON).get(ConfigurationEntity.class);
                } catch (RuntimeException e) {
                    throw new DownloadKeyException("Unable to download the jwks_uri, configuration URL:" + configurationUrl, e);
                }

                try {
                    configurationURLs = new ConfigurationURLs(configurationEntity);
                } catch (MalformedURLException e) {
                    throw new DownloadKeyException("jwks_uri is not a valid URI, configuration URL:\" + configurationUrl", e);
                }
                configurationURLsCache.put(configurationUrl, configurationURLs);
            }
            return configurationURLs;
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
    }

    private AccessToken(String issuer) {
        this.issuer = issuer;
    }

    public String getIssuer() { return issuer; }
}
