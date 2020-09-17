package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.metadata.OidcMetadata;

import javax.cache.Cache;
import javax.cache.integration.CacheLoader;
import java.security.interfaces.RSAPublicKey;

public interface CacheProvider {

  Cache<String, OidcMetadata> createOidcConfigurationCache(
      CacheLoader<String, OidcMetadata> cacheLoader);

  Cache<String, RSAPublicKey> createPublicJwkCache(CacheLoader<String, RSAPublicKey> cacheLoader);
}
