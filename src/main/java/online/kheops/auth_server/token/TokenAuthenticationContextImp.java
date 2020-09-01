package online.kheops.auth_server.token;

import online.kheops.auth_server.fetch.FetchContextListener;
import online.kheops.auth_server.report_provider.OidcProvider;
import online.kheops.auth_server.report_provider.OidcProviderImp;
import online.kheops.auth_server.report_provider.ReportProviderCatalogue;
import online.kheops.auth_server.report_provider.ReportProviderCatalogueImp;
import online.kheops.auth_server.report_provider.metadata.OidcMetadata;
import online.kheops.auth_server.report_provider.metadata.ParameterMapReader;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TokenAuthenticationContextImp implements TokenAuthenticationContext {
  private final Client client;
  private final ServletContext servletContext;
  private final ReportProviderCatalogue reportProviderCatalogue;
  private final OidcProvider oidcProvider;

  private static CacheManager getCacheManager() {
    CachingProvider cachingProvider = Caching.getCachingProvider();
    return cachingProvider.getCacheManager();
  }

  private Cache<String, OidcMetadata> getOidcMetadataCache() {

    MutableConfiguration<String, OidcMetadata> configuration =
        new MutableConfiguration<String, OidcMetadata>()
            .setTypes(String.class, OidcMetadata.class)
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES))
            .setReadThrough(true)
            .setCacheLoaderFactory(
                new FactoryBuilder.SingletonFactory<>(OidcProviderImp.getOidcMetadataCacheLoader(client)));

    return getCacheManager().createCache("oidc_metadata_cache", configuration);
  }

  private Cache<String, RSAPublicKey> getPublicJwkCache() {

    MutableConfiguration<String, RSAPublicKey> configuration =
        new MutableConfiguration<String, RSAPublicKey>()
            .setTypes(String.class, RSAPublicKey.class)
            .setStoreByValue(false)
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES))
            .setReadThrough(true)
            .setCacheLoaderFactory(new FactoryBuilder.SingletonFactory<>(OidcProviderImp.getPublicJwkCacheLoader(client)));

    return getCacheManager().createCache("jwk_public_key_cache", configuration);
  }


  public TokenAuthenticationContextImp(ServletContext servletContext) {
    this.servletContext = Objects.requireNonNull(servletContext);
    reportProviderCatalogue = new ReportProviderCatalogueImp();
    client = ClientBuilder.newClient().register(ParameterMapReader.class);
    oidcProvider = new OidcProviderImp(this, client, getOidcMetadataCache(), getPublicJwkCache());
  }

  @Override
  public ServletContext getServletContext() {
    return servletContext;
  }

  @Override
  public ReportProviderCatalogue getReportProviderCatalogue() {
    return reportProviderCatalogue;
  }

  @Override
  public TokenBasicAuthenticator newTokenBasicAuthenticator() {
    return TokenBasicAuthenticator.newAuthenticator(servletContext);
  }

  @Override
  public PrivateKeyJWTAuthenticator newPrivateKeyJWTAuthenticator() {
    return PrivateKeyJWTAuthenticator.newAuthenticator(servletContext);
  }

  @Override
  public String getOIDCProvider() {
    return servletContext.getInitParameter("online.kheops.oidc.provider");
  }

  @Override
  public URI getOIDCConfigurationUri() {
    try {
      return new URI(getOIDCProvider() + "/.well-known/openid-configuration");
    } catch (URISyntaxException e) {
      throw new AssertionError("Unable to build an OIDC configuration URI", e);
    }
  }

  @Override
  public OidcProvider getOidcProvider() {
    return oidcProvider;
  }
}
