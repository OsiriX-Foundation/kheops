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

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.servlet.ServletContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalUriParameter.*;

public class OidcProviderImp implements OidcProvider {

  public static CacheLoader<String, OidcMetadata> getOidcMetadataCacheLoader(Client client) {
    return new OidcMetadataLoader(client);
  }

  public static CacheLoader<String, RSAPublicKey> getPublicJwkCacheLoader(Client client) {
    return new JwkPublicKeyLoader(client);
  }

  private final ServletContext servletContext;
  private final Client client;
  private final Cache<String, OidcMetadata> configurationCache;
  private final Cache<String, RSAPublicKey> publicJwkCache;

  public OidcProviderImp(
      ServletContext servletContext,
      Client client,
      Cache<String, OidcMetadata> configurationCache,
      Cache<String, RSAPublicKey> publicJwkCache) {
    this.servletContext = servletContext;
    this.client = client;
    this.configurationCache = configurationCache;
    this.publicJwkCache = publicJwkCache;
  }

  private abstract static class Loader<V> implements CacheLoader<String, V> {
    final Client client;

    Loader(Client client) {
      this.client = client;
    }

    public Map<String, V> loadAll(Iterable<? extends String> keys) {
      Map<String, V> data = new HashMap<>();
      for (String key : keys) {
        data.put(key, load(key));
      }
      return data;
    }
  }

  private static class OidcMetadataLoader extends Loader<OidcMetadata> {

    OidcMetadataLoader(Client client) {
      super(client);
    }

    public OidcMetadata load(String key) {
      return client.target(URI.create(key)).request(APPLICATION_JSON_TYPE).get(ParameterMap.class);
    }
  }

  private static class JwkPublicKeyLoader extends Loader<RSAPublicKey> {

    JwkPublicKeyLoader(Client client) {
      super(client);
    }

    public RSAPublicKey load(String key) throws CacheLoaderException {
      String[] urlAndId = key.split(" ", 2);
      try {
        return (RSAPublicKey)
            new UrlJwkProvider(new URL(urlAndId[0])).get(urlAndId[1]).getPublicKey();
      } catch (MalformedURLException | JwkException e) {
        throw new CacheLoaderException("Unable to get JWK", e);
      }
    }
  }

  @Override
  public OidcMetadata getOidcMetadata() throws OidcProviderException {
    try {
      return configurationCache.get(getOIDCConfigurationUri().toString());
    } catch (CacheException e) {
      throw new OidcProviderException("unable to get metadata", e);
    }
  }

  @Override
  public OidcMetadata getUserInfo(String token) throws OidcProviderException {
    try {
      return client
          .target(
              getOidcMetadata()
                  .getValue(USERINFO_ENDPOINT)
                  .orElseThrow(() -> new OidcProviderException("No Userinfo endpoint")))
          .request(APPLICATION_JSON_TYPE)
          .header("Authorization", "Bearer " + token)
          .get(ParameterMap.class);
    } catch (WebApplicationException | ProcessingException e) {
      throw new OidcProviderException("Unable to get user info", e);
    }
  }

  @Override
  public DecodedJWT validateAccessToken(String accessToken, boolean verifySignature) throws OidcProviderException {
    final URI jwksUri =
        getOidcMetadata()
            .getValue(JWKS_URI)
            .orElseThrow(() -> new OidcProviderException("No jwks_uri"));
    final URI issuer =
        getOidcMetadata()
            .getValue(ISSUER)
            .orElseThrow(() -> new OidcProviderException("No issuer"));

    final RSAKeyProvider keyProvider =
        new RSAKeyProvider() {
          @Override
          public RSAPublicKey getPublicKeyById(String keyId) {
            return publicJwkCache.get(jwksUri.toString() + " " + keyId);
          }

          @Override
          public RSAPrivateKey getPrivateKey() {
            return null;
          }

          @Override
          public String getPrivateKeyId() {
            return null;
          }
        };

    final DecodedJWT jwt;
    try {
      if (verifySignature) {
        jwt = JWT.require(Algorithm.RSA256(keyProvider))
            .acceptLeeway(120)
            .withIssuer(issuer.toString())
            .acceptLeeway(60)
            .build()
            .verify(accessToken);
      } else {
        if (accessToken.indexOf('.') == -1) {
          jwt = JWT.decode("eyJhbGciOiJub25lIn0." + accessToken + ".");
        } else {
          jwt = JWT.decode(accessToken);
        }
      }
    } catch (JWTVerificationException e) {
      throw new OidcProviderException(
          "Verification of the token failed, configuration URL:" + getOIDCConfigurationUri(), e);
    }

    if (jwt.getSubject() == null) {
      throw new OidcProviderException(
          "No subject present in the token, configuration URL:" + getOIDCConfigurationUri());
    }

    final String oauthScope = getOauthScope();
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

  private URI getOIDCConfigurationUri() {
    try {
      return new URI(getOIDCProviderUriString() + "/.well-known/openid-configuration");
    } catch (URISyntaxException e) {
      throw new AssertionError("Unable to build an OIDC configuration URI", e);
    }
  }

  private String getOIDCProviderUriString() {
    return servletContext.getInitParameter("online.kheops.oidc.provider");
  }

  private String getOauthScope() {
    return servletContext.getInitParameter("online.kheops.oauth.scope");
  }
}
