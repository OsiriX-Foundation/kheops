package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.report_provider.metadata.ParameterMapReader;
import online.kheops.auth_server.token.CacheProvider;
import org.glassfish.hk2.api.Factory;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;

public class OidcProviderFactory implements Factory<OidcProvider> {

  @Context private CacheProvider cacheProvider;

  @Context private ServletContext servletContext;

  @Override
  public OidcProvider provide() {
    Client client = ClientBuilder.newClient().register(ParameterMapReader.class);
    return new OidcProviderImp(
        servletContext,
        client,
        cacheProvider.createOidcConfigurationCache(
            OidcProviderImp.getOidcMetadataCacheLoader(client)),
        cacheProvider.createPublicJwkCache(OidcProviderImp.getPublicJwkCacheLoader(client)));
  }

  @Override
  public void dispose(OidcProvider instance) {}
}
