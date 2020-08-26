package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.report_provider.metadata.OidcMetadata;
import online.kheops.auth_server.report_provider.metadata.Parameter;
import online.kheops.auth_server.report_provider.metadata.ParameterMap;
import online.kheops.auth_server.report_provider.metadata.ParameterMapReader;
import online.kheops.auth_server.token.TokenAuthenticationContext;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class OidcProviderImp implements OidcProvider {

  private static final Client CLIENT = ClientBuilder.newClient().register(ParameterMapReader.class);

  private final TokenAuthenticationContext context;

  private OidcMetadata oidcMetadata;

  public OidcProviderImp(TokenAuthenticationContext context) {
    this.context = context;
  }

  private void loadMetadata() throws OidcProviderException {
    if (oidcMetadata == null) {
      try {
        oidcMetadata =
            CLIENT
                .target(context.getOIDCConfigurationString())
                .request(APPLICATION_JSON_TYPE)
                .get(ParameterMap.class);
      } catch (ProcessingException | WebApplicationException e) {
        throw new OidcProviderException("Unable to load metadata", e);
      }
    }
  }

  @Override
  public OidcMetadata getOidcMetadata() throws OidcProviderException {
    loadMetadata();

    return oidcMetadata;
  }

  @Override
  public Map<String, String> getUserInfo(String token) throws OidcProviderException {
    loadMetadata();

    // TODO
    throw new OidcProviderException();
  }
}
