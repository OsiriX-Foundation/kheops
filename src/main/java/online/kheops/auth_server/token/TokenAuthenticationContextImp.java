package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.ReportProviderCatalogue;
import online.kheops.auth_server.report_provider.ReportProviderCatalogueImp;

import javax.servlet.ServletContext;
import java.util.Objects;

public class TokenAuthenticationContextImp implements TokenAuthenticationContext {
  private final ServletContext servletContext;
  private final ReportProviderCatalogue reportProviderCatalogue;

  public TokenAuthenticationContextImp(ServletContext servletContext) {
    this.servletContext = Objects.requireNonNull(servletContext);
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
}
