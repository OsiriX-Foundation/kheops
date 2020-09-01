package online.kheops.auth_server.report_provider;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import online.kheops.auth_server.report_provider.metadata.OidcMetadata;
import online.kheops.auth_server.report_provider.metadata.ParameterMap;
import online.kheops.auth_server.report_provider.metadata.ParameterMapReader;
import online.kheops.auth_server.token.TokenAuthenticationContext;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.spi.CachingProvider;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalUriParameter.*;

public class OidcProviderImp implements OidcProvider {

  public static final CacheLoader<String, OidcMetadata> OIDC_METADATA_CACHE_LOADER = new OidcMetadataLoader();
  public static final CacheLoader<String, RSAPublicKey> PUBLIC_JWK_CACHE_LOADER = new JwkPublicKeyLoader();

  private static final Client CLIENT = ClientBuilder.newClient().register(ParameterMapReader.class);

  private final TokenAuthenticationContext context;

  public OidcProviderImp(TokenAuthenticationContext context) {
    this.context = context;
  }

  private static final Cache<String, OidcMetadata> OIDC_METADATA_CACHE = getOidcMetadataCache();
  private static final Cache<String, RSAPublicKey> PUBLIC_JWK_CACHE = getPublicJwkCache();

  private static CacheManager getCacheManager() {
    CachingProvider cachingProvider = Caching.getCachingProvider();
    return cachingProvider.getCacheManager();
  }

  private static Cache<String, OidcMetadata> getOidcMetadataCache() {

    MutableConfiguration<String, OidcMetadata> configuration =
        new MutableConfiguration<String, OidcMetadata>()
            .setTypes(String.class, OidcMetadata.class)
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES))
            .setReadThrough(true)
            .setCacheLoaderFactory(new FactoryBuilder.SingletonFactory<>(OIDC_METADATA_CACHE_LOADER));

    return getCacheManager().createCache("oidc_metadata_cache", configuration);
  }

  private static Cache<String, RSAPublicKey> getPublicJwkCache() {

    MutableConfiguration<String, RSAPublicKey> configuration =
        new MutableConfiguration<String, RSAPublicKey>()
            .setTypes(String.class, RSAPublicKey.class)
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES))
            .setReadThrough(true)
            .setCacheLoaderFactory(new FactoryBuilder.SingletonFactory<>(PUBLIC_JWK_CACHE_LOADER));

    return getCacheManager().createCache("jwk_public_key_cache", configuration);
  }

  private static class OidcMetadataLoader
      implements CacheLoader<String, OidcMetadata> {

    public OidcMetadata load(String key) {
      return CLIENT
          .target(URI.create(key))
          .request(APPLICATION_JSON_TYPE)
          .get(ParameterMap.class);
    }

    public Map<String, OidcMetadata> loadAll(Iterable<? extends String> keys) {
      Map<String, OidcMetadata> data = new HashMap<>();
      for (String key : keys) {
        data.put(key, load(key));
      }
      return data;
    }
  }

  private static class JwkPublicKeyLoader
      implements CacheLoader<String, RSAPublicKey> {

    public RSAPublicKey load(String key) throws CacheLoaderException  {
      String[] urlAndId = key.split(" ", 2);
      try {
        return (RSAPublicKey) new UrlJwkProvider(new URL(urlAndId[0])).get(urlAndId[1]).getPublicKey();
      } catch (MalformedURLException | JwkException e) {
        throw new CacheLoaderException("Unable to get JWK", e);
      }
    }

    public Map<String, RSAPublicKey> loadAll(Iterable<? extends String> keys) {
      Map<String, RSAPublicKey> data = new HashMap<>();
      for (String key : keys) {
        data.put(key, load(key));
      }
      return data;
    }
  }

  @Override
  public OidcMetadata getOidcMetadata() throws OidcProviderException {
    try {
      return OIDC_METADATA_CACHE.get(context.getOIDCConfigurationString());
    } catch (CacheException e) {
      throw new OidcProviderException("unable to get metadata", e);
    }
  }

  @Override
  public OidcMetadata getUserInfo(String token) throws OidcProviderException {
    try {
      return CLIENT
          .target(getOidcMetadata().getValue(USERINFO_ENDPOINT)
              .orElseThrow(() -> new OidcProviderException("No Userinfo endpoint")))
          .request(APPLICATION_JSON_TYPE)
          .header("Authorization", "Bearer " + token)
          .get(ParameterMap.class);
    } catch (WebApplicationException | ProcessingException e) {
      throw new OidcProviderException("Unable to get user info", e);
    }
  }

  @Override
  public DecodedJWT validateAccessToken(String accessToken) throws OidcProviderException {
    final URI jwksUri = getOidcMetadata().getValue(JWKS_URI)
        .orElseThrow(() -> new OidcProviderException("No jwks_uri"));
    final URI issuer = getOidcMetadata().getValue(ISSUER)
        .orElseThrow(() -> new OidcProviderException("No issuer"));

    final RSAKeyProvider keyProvider = new RSAKeyProvider() {
      @Override
      public RSAPublicKey getPublicKeyById(String keyId) {
        return PUBLIC_JWK_CACHE.get(jwksUri.toString() + " " + keyId);
      }
      // implemented to get rid of warnings
      @Override public RSAPrivateKey getPrivateKey() {return null;}
      @Override public String getPrivateKeyId() {return null;}
    };

    final DecodedJWT jwt;
    try {
      jwt = JWT.require(Algorithm.RSA256(keyProvider))
          .acceptLeeway(120)
          .withIssuer(issuer.toString())
          .acceptLeeway(60)
          .build()
          .verify(accessToken);
    } catch (JWTVerificationException e) {
      throw new OidcProviderException("Verification of the token failed, configuration URL:" + context.getOIDCConfigurationString(), e);
    }

    if (jwt.getSubject() == null) {
      throw new OidcProviderException("No subject present in the token, configuration URL:" + context.getOIDCConfigurationString());
    }

    final String oauthScope = context.getServletContext().getInitParameter("online.kheops.oauth.scope");
    if (oauthScope != null && !oauthScope.isEmpty()) {
      final Claim scopeClaim = jwt.getClaim("scope");
      if (scopeClaim.isNull() || scopeClaim.asString() == null) {
        throw new OidcProviderException("Missing scope claim in token");
      } else {
        final String[] words = scopeClaim.asString().split("\\s+", 40);
        if (!Arrays.asList(words).contains(oauthScope)) {
          throw new OidcProviderException("Missing required scope in token");
        }
      }
    }

    return jwt;
  }
}
