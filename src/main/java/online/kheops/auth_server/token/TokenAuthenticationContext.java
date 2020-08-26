package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.OidcProvider;
import online.kheops.auth_server.report_provider.ReportProviderCatalogue;
import online.kheops.auth_server.report_provider.metadata.OidcMetadata;

import javax.servlet.ServletContext;

public interface TokenAuthenticationContext {
  ServletContext getServletContext();
  ReportProviderCatalogue getReportProviderCatalogue();
  TokenBasicAuthenticator newTokenBasicAuthenticator();
  PrivateKeyJWTAuthenticator newPrivateKeyJWTAuthenticator();

  OidcProvider getOidcProvider();

  String getOIDCProvider();
  String getOIDCConfigurationString();
}
