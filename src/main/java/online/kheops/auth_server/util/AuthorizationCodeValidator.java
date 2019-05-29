package online.kheops.auth_server.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.servlet.ServletContext;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class AuthorizationCodeValidator {
    private static final String HOST_ROOT_PARAMETER = "online.kheops.root.uri";
    private static final String HMAC_SECRET_PARAMETER = "online.kheops.auth.hmacsecret";

    private final ServletContext servletContext;
    private String clientId;

    public static AuthorizationCodeValidator createAuthorizer(ServletContext servletContext) {
        return new AuthorizationCodeValidator(servletContext);
    }

    private AuthorizationCodeValidator(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public AuthorizationCodeValidator withClientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    public DecodedAuthorizationCode validate(final String authorizationCode) throws TokenAuthenticationException {
        final DecodedJWT jwt;
        try {
            jwt = JWT.require(Algorithm.HMAC256(getHMAC256Secret()))
                    .withIssuer(getIssuerHost())
                    .withAudience(getIssuerHost())
                    .withClaim("azp", Objects.requireNonNull(clientId))
                    .withClaim("type", "report_provider_code")
                    .build()
                    .verify(authorizationCode);
        } catch (JWTVerificationException | UnsupportedEncodingException e) {
            throw new TokenAuthenticationException("Unable to validate the authorization code", e);
        }

        final List<String> studyUIs;
        try {
            studyUIs = jwt.getClaim("study_uids").asList(String.class);
        } catch (JWTDecodeException e) {
            throw new TokenAuthenticationException("can't read study UIDs", e);
        }
        if (studyUIs == null) {
            throw new TokenAuthenticationException("can't find study UIDs");
        }

        final String subject;
        try {
            subject = jwt.getSubject();
        } catch (JWTDecodeException e) {
            throw new TokenAuthenticationException("can't read Subject", e);
        }
        if (subject == null) {
            throw new TokenAuthenticationException("can't find Subject");
        }

        return DecodedAuthorizationCode.createDecodedAuthorizationCode(subject, new HashSet<>(studyUIs));
    }

    private String getHMAC256Secret() {
        return servletContext.getInitParameter(HMAC_SECRET_PARAMETER);
    }

    private String getIssuerHost() {
        return servletContext.getInitParameter(HOST_ROOT_PARAMETER);
    }
}
