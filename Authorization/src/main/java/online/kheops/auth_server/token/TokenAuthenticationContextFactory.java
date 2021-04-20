package online.kheops.auth_server.token;

import online.kheops.auth_server.report_provider.OidcProvider;
import org.glassfish.hk2.api.Factory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

public class TokenAuthenticationContextFactory implements Factory<TokenAuthenticationContext> {

  @Context
  private ServletContext servletContext;

  @Context
  private OidcProvider oidcProvider;

  @Override
  public TokenAuthenticationContext provide() {
    return new TokenAuthenticationContextImp(servletContext, oidcProvider);
  }

  @Override
  public void dispose(TokenAuthenticationContext instance) {}
}
