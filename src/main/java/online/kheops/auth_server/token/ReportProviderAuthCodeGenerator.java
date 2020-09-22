package online.kheops.auth_server.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import online.kheops.auth_server.util.JWTs;

import javax.servlet.ServletContext;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class ReportProviderAuthCodeGenerator {
  private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

  private final ServletContext servletContext;

  private DecodedToken decodedToken;
  private String codeChallenge;
  private String nonce;
  private String state;
  private URI redirectUri;

  private ReportProviderAuthCodeGenerator(final ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public static ReportProviderAuthCodeGenerator createGenerator(final ServletContext servletContext) {
    return new ReportProviderAuthCodeGenerator(servletContext);
  }

  public ReportProviderAuthCodeGenerator withDecodedLoginHint(final DecodedToken decodedToken) {
    this.decodedToken = Objects.requireNonNull(decodedToken);
    return this;
  }

  public ReportProviderAuthCodeGenerator withCodeChallenge(final String codeChallenge) {
    this.codeChallenge = Objects.requireNonNull(codeChallenge);
    return this;
  }

  public ReportProviderAuthCodeGenerator withState(final String state) {
    this.state = Objects.requireNonNull(state);
    return this;
  }

  public ReportProviderAuthCodeGenerator withNonce(final String nonce) {
    this.nonce = Objects.requireNonNull(nonce);
    return this;
  }

  public ReportProviderAuthCodeGenerator withRedirectUri(final URI redirectUri) {
    this.redirectUri = Objects.requireNonNull(redirectUri);
    return this;
  }

  public String generate(@SuppressWarnings("SameParameterValue") long expiresIn) {
    Objects.requireNonNull(decodedToken);
    Objects.requireNonNull(codeChallenge);

    final String authSecret = getHMACSecret();
    final Algorithm algorithmHMAC;
    try {
      algorithmHMAC = Algorithm.HMAC256(authSecret);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("online.kheops.auth.hmacsecret is not a valid HMAC secret", e);
    }

    final JWTCreator.Builder jwtBuilder = JWT.create()
        .withExpiresAt(Date.from(Instant.now().plus(expiresIn, ChronoUnit.SECONDS)))
        .withNotBefore(new Date())
        .withArrayClaim("study_uids", decodedToken.getStudyInstanceUIDs().toArray(new String[0]))
        .withSubject(Objects.requireNonNull(decodedToken.getSubject()))
        .withIssuer(getHostRoot())
        .withAudience(getHostRoot())
        .withClaim("azp", Objects.requireNonNull(decodedToken.getClientId()))
        .withClaim("type", "report_provider_code");

    JWTs.encodeSource(jwtBuilder, decodedToken.getSource());

    decodedToken.getActingParty().ifPresent(actingParty -> jwtBuilder.withClaim("act", actingParty));
    decodedToken.getCapabilityTokenId().ifPresent(capabilityTokenId -> jwtBuilder.withClaim("cap_token", capabilityTokenId));

    if (codeChallenge != null) {
      jwtBuilder.withClaim("code_challenge", codeChallenge);
    }

    if (nonce != null) {
      jwtBuilder.withClaim("nonce", nonce);
    }

    if (state != null) {
      jwtBuilder.withClaim("state", state);
    }

    if (redirectUri != null) {
      jwtBuilder.withClaim("redirect_uri", redirectUri.toString());
    }

    return jwtBuilder.sign(algorithmHMAC);
  }

  private String getHostRoot() {
    return Objects.requireNonNull(servletContext).getInitParameter(HOST_ROOT_PARAMETER);
  }

  private String getHMACSecret() {
    return Objects.requireNonNull(servletContext).getInitParameter(HMAC_SECRET_PARAMETER);
  }
}
