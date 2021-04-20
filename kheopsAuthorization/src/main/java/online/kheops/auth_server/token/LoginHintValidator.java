package online.kheops.auth_server.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import online.kheops.auth_server.util.JWTs;
import online.kheops.auth_server.util.Source;

import javax.servlet.ServletContext;
import java.util.*;

import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_REQUEST;
import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class LoginHintValidator {
  private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

  private final ServletContext servletContext;
  private String clientId;

  public static LoginHintValidator createAuthorizer(ServletContext servletContext) {
    return new LoginHintValidator(servletContext);
  }

  private LoginHintValidator(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public LoginHintValidator withClientId(final String clientId) {
    this.clientId = Objects.requireNonNull(clientId);
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

    final String email;
    Claim emailClaim = jwt.getClaim("email");
    if (!emailClaim.isNull()) {
      email = emailClaim.asString();
    } else {
      email = null;
    }

    final Boolean oidcInitiated;
    Claim oidcInitiatedClaim = jwt.getClaim("oidcInitiated");
    if (!oidcInitiatedClaim.isNull()) {
      oidcInitiated = oidcInitiatedClaim.asBoolean();
    } else {
      oidcInitiated = null;
    }

    return DecodedToken.createDecodedLoginHint(
        subject, clientId, actingParty, capabilityTokenId, new HashSet<>(studyUIs), source, email, oidcInitiated);
  }

  private String getHMAC256Secret() {
    return servletContext.getInitParameter(HMAC_SECRET_PARAMETER);
  }

  private String getIssuerHost() {
    return servletContext.getInitParameter(HOST_ROOT_PARAMETER);
  }
}
