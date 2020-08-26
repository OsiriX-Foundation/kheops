package online.kheops.auth_server.accesstoken;

import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.principal.KheopsPrincipal;
import online.kheops.auth_server.token.TokenAuthenticationContext;
import online.kheops.auth_server.token.TokenProvenance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccessToken extends TokenProvenance {

  enum TokenType {
    KEYCLOAK_TOKEN,
    ALBUM_CAPABILITY_TOKEN,
    USER_CAPABILITY_TOKEN,
    PEP_TOKEN,
    VIEWER_TOKEN,
    REPORT_PROVIDER_TOKEN
  }

  String getSubject();

  TokenType getTokenType();

  KheopsPrincipal newPrincipal(TokenAuthenticationContext tokenAuthenticationContext, User user);

  default Optional<String> getScope() {
    return Optional.empty();
  }

  default Optional<String> getClientId() {
    return Optional.empty();
  }

  default Optional<List<String>> getStudyUIDs() {
    return Optional.empty();
  }

  default Optional<Instant> getExpiresAt() {
    return Optional.empty();
  }

  default Optional<Instant> getIssuedAt() {
    return Optional.empty();
  }

  default Optional<Instant> getNotBefore() {
    return Optional.empty();
  }

  default Optional<List<String>> getAudience() {
    return Optional.empty();
  }

  default Optional<String> getIssuer() {
    return Optional.empty();
  }
}
