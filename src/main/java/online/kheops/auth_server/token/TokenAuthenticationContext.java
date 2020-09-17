package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.OidcProvider;
import online.kheops.auth_server.report_provider.ReportProviderCatalogue;

import javax.servlet.ServletContext;
import java.net.URI;

public interface TokenAuthenticationContext {
  ServletContext getServletContext();
  ReportProviderCatalogue getReportProviderCatalogue();
  TokenBasicAuthenticator newTokenBasicAuthenticator();
  PrivateKeyJWTAuthenticator newPrivateKeyJWTAuthenticator();

  OidcProvider getOidcProvider();
}
