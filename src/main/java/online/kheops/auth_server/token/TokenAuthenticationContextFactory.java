package online.kheops.auth_server.token;

import org.glassfish.hk2.api.Factory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

public class TokenAuthenticationContextFactory implements Factory<TokenAuthenticationContext> {

  @Context
  private ServletContext servletContext;

  @Override
  public TokenAuthenticationContext provide() {
    return new TokenAuthenticationContextImp(servletContext);
  }

  @Override
  public void dispose(TokenAuthenticationContext instance) {}
}
