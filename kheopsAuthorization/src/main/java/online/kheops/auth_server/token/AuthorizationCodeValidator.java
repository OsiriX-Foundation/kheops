package online.kheops.auth_server.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.servlet.ServletContext;
import java.util.*;

import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_REQUEST;
import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

class AuthorizationCodeValidator {
    private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

    private final ServletContext servletContext;
    private String clientId;

    static AuthorizationCodeValidator createAuthorizer(ServletContext servletContext) {
        return new AuthorizationCodeValidator(servletContext);
    }

    private AuthorizationCodeValidator(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    AuthorizationCodeValidator withClientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    DecodedAuthorizationCode validate(final String authorizationCode) {
        final DecodedJWT jwt;
        try {
            jwt = JWT.require(Algorithm.HMAC256(getHMAC256Secret()))
                    .withIssuer(getIssuerHost())
                    .withAudience(getIssuerHost())
                    .withClaim("azp", Objects.requireNonNull(clientId))
                    .withClaim("type", "report_provider_code")
                    .acceptLeeway(60)
                    .build()
                    .verify(authorizationCode);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new TokenRequestException(INVALID_REQUEST, "Unable to validate the authorization code", e);
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

        return DecodedAuthorizationCode.createDecodedAuthorizationCode(subject, actingParty, capabilityTokenId, new HashSet<>(studyUIs));
    }

    private String getHMAC256Secret() {
        return servletContext.getInitParameter(HMAC_SECRET_PARAMETER);
    }

    private String getIssuerHost() {
        return servletContext.getInitParameter(HOST_ROOT_PARAMETER);
    }
}
