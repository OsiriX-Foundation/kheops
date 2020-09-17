package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.OidcProvider;
import online.kheops.auth_server.report_provider.ReportProviderCatalogue;
import online.kheops.auth_server.report_provider.ReportProviderCatalogueImp;

import javax.servlet.ServletContext;
import java.util.Objects;

public class TokenAuthenticationContextImp implements TokenAuthenticationContext {
  private final ServletContext servletContext;
  private final ReportProviderCatalogue reportProviderCatalogue;
  private final OidcProvider oidcProvider;

  public TokenAuthenticationContextImp(ServletContext servletContext, OidcProvider oidcProvider) {
    this.servletContext = Objects.requireNonNull(servletContext);
    this.oidcProvider = oidcProvider;
    reportProviderCatalogue = new ReportProviderCatalogueImp();
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
  public OidcProvider getOidcProvider() {
    return oidcProvider;
  }
}
