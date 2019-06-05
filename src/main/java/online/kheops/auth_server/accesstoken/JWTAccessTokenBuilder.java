package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import online.kheops.auth_server.keycloak.KeycloakContextListener;

import javax.servlet.ServletContext;

final class JWTAccessTokenBuilder extends AccessTokenBuilder {
    private static final String HOST_ROOT_PARAMETER = "online.kheops.root.uri";
    private static final String SUPERUSER_ISSUER = "authorization.kheops.online";
    private static final String KHEOPS_ISSUER = "auth.kheops.online";

    private final String issuerHost;

    JWTAccessTokenBuilder(ServletContext servletContext) {
        super(servletContext);
        this.issuerHost = getIssuerHost();
    }

    @Override
    public AccessToken build(String assertionToken) throws AccessTokenVerificationException {
        final String issuer;

        try {
            issuer = JWT.decode(assertionToken).getIssuer();
        } catch (JWTDecodeException | NullPointerException e) {
            throw new AccessTokenVerificationException("Unable to decode JWT", e);
        }

        if (issuer == null) {
            throw new AccessTokenVerificationException("JWT has no issuer");
        }

        if (issuer.equals(KeycloakContextListener.getKeycloakIssuer())) {
            return JWTAccessToken.getBuilder(KeycloakContextListener.getKeycloakOIDCConfigurationString()).build(assertionToken);
        } else if (issuer.equals(KHEOPS_ISSUER)) {
            return AuthorizationJWTAccessToken.getBuilder(authorizationSecret()).build(assertionToken);
        } else if (issuer.equals(SUPERUSER_ISSUER)) {
            return SuperuserJWTAccessToken.getBuilder(superuserSecret()).build(assertionToken);
        } else if (issuer.equals(issuerHost)) {
            return ReportProviderAccessToken.getBuilder(getServletContext()).build(assertionToken);
        } else {
            throw new AccessTokenVerificationException("Unknown JWT Issuer:" + issuer);
        }
    }

    private String authorizationSecret() {
        return getServletContext().getInitParameter("online.kheops.auth.hmacsecret");
    }

    private String superuserSecret() {
        return getServletContext().getInitParameter("online.kheops.superuser.hmacsecret");
    }

    private String getIssuerHost() {
        return getServletContext().getInitParameter(HOST_ROOT_PARAMETER);
    }
}
