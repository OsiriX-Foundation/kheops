package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.metadata.OidcMetadata;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.integration.CacheLoader;
import javax.cache.spi.CachingProvider;
import java.security.interfaces.RSAPublicKey;

public class CacheProviderImp implements CacheProvider {

  @Override
  public Cache<String, OidcMetadata> createOidcConfigurationCache(CacheLoader<String, OidcMetadata> cacheLoader) {

    MutableConfiguration<String, OidcMetadata> configuration =
        new MutableConfiguration<String, OidcMetadata>()
            .setTypes(String.class, OidcMetadata.class)
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES))
            .setReadThrough(true)
            .setCacheLoaderFactory(
                new FactoryBuilder.SingletonFactory<>(cacheLoader));

    return getCacheManager().createCache("oidc_metadata_cache", configuration);
  }

  @Override
  public Cache<String, RSAPublicKey> createPublicJwkCache(CacheLoader<String, RSAPublicKey> cacheLoader) {

    MutableConfiguration<String, RSAPublicKey> configuration =
        new MutableConfiguration<String, RSAPublicKey>()
            .setTypes(String.class, RSAPublicKey.class)
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES))
            .setReadThrough(true)
            .setCacheLoaderFactory(new FactoryBuilder.SingletonFactory<>(cacheLoader));

    return getCacheManager().createCache("jwk_public_key_cache", configuration);
  }

  private static CacheManager getCacheManager() {
    CachingProvider cachingProvider = Caching.getCachingProvider();
    return cachingProvider.getCacheManager();
  }
}
