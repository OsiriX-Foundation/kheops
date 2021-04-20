package online.kheops.auth_server.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.util.JWTs;
import online.kheops.auth_server.util.Source;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_REQUEST;
import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class AuthCodeValidator {
  private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

  private final TokenAuthenticationContext tokenAthenticationContext;
  private String codeVerifier;
  private String clientId;

  public static AuthCodeValidator createAuthorizer(TokenAuthenticationContext tokenAuthenticationContext) {
    return new AuthCodeValidator(tokenAuthenticationContext);
  }

  private AuthCodeValidator(TokenAuthenticationContext tokenAthenticationContext) {
    this.tokenAthenticationContext = tokenAthenticationContext;
  }

  public AuthCodeValidator withClientId(final String clientId) {
    this.clientId = Objects.requireNonNull(clientId);
    return this;
  }

  public AuthCodeValidator withCodeVerifier(final String codeVerifier) {
    this.codeVerifier = Objects.requireNonNull(codeVerifier);
    return this;
  }

  public DecodedToken validate(final String loginHint) {
    final DecodedJWT jwt;
    try {
      jwt =
          JWT.require(Algorithm.HMAC256(getHMAC256Secret()))
              .withIssuer(getIssuerHost())
              .withAudience(getIssuerHost())
              .withClaim("azp", Objects.requireNonNull(clientId))
              .withClaim("type", "report_provider_code")
              .acceptLeeway(60)
              .build()
              .verify(loginHint);
    } catch (JWTVerificationException | IllegalArgumentException e) {
      throw new TokenRequestException(
          INVALID_REQUEST, "Unable to validate", e);
    }

    Claim codeChallengeClaim = jwt.getClaim("code_challenge");
    if (!codeChallengeClaim.isNull()) {
      if (codeVerifier == null) {
        throw new TokenRequestException(INVALID_REQUEST, "Unable to validate");
      }

      final String codeChallenge = codeChallengeClaim.asString();
      final MessageDigest digest;
      try {
        digest = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException e) {
        throw new AssertionError(e);
      }
      final byte[] encodedHash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));

      // base64 encode without padding https://tools.ietf.org/html/rfc7636#appendix-A
      String verifierString = Base64.getEncoder().encodeToString(encodedHash);
      verifierString = verifierString.split("=")[0]; // Remove any trailing '='s
      verifierString = verifierString.replace('+', '-'); // 62nd char of encoding
      verifierString = verifierString.replace('/', '_'); // 63rd char of encoding

      if (!codeChallenge.equals(verifierString)) {
        throw new TokenRequestException(INVALID_REQUEST, "Unable to validate code challenge");
      }
    }

    final List<String> studyUIs;
    try {
      studyUIs = jwt.getClaim("study_uids").asList(String.class);
    } catch (JWTDecodeException e) {
      throw new TokenRequestException(INVALID_REQUEST, "Can't read study UIDs", e);
    }
    if (studyUIs == null) {
      throw new TokenRequestException(INVALID_REQUEST, "Can't find study UIDs");
    }
    final Source source;
    try {
      source = JWTs.decodeSource(jwt);
    } catch (JWTDecodeException e) {
      throw new TokenRequestException(INVALID_REQUEST, "Can't read source", e);
    }

    final String subject;
    try {
      subject = jwt.getSubject();
    } catch (JWTDecodeException e) {
      throw new TokenRequestException(INVALID_REQUEST, "Can't read subject", e);
    }
    if (subject == null) {
      throw new TokenRequestException(INVALID_REQUEST, "Can't find subject");
    }

    final String actingParty;
    Claim actClaim = jwt.getClaim("act");
    if (!actClaim.isNull()) {
      actingParty = actClaim.asString();
    } else {
      actingParty = null;
    }

    final String capabilityTokenId;
    Claim capabilityTokenClaim = jwt.getClaim("cap_token");
    if (!capabilityTokenClaim.isNull()) {
      capabilityTokenId = capabilityTokenClaim.asString();
    } else {
      capabilityTokenId = null;
    }

    return DecodedToken.createDecodedLoginHint(
        subject, clientId, actingParty, capabilityTokenId, new HashSet<>(studyUIs), source, null, null);
  }

  private String getHMAC256Secret() {
    return tokenAthenticationContext.getServletContext().getInitParameter(HMAC_SECRET_PARAMETER);
  }

  private String getIssuerHost() {
    return tokenAthenticationContext.getServletContext().getInitParameter(HOST_ROOT_PARAMETER);
  }
}
