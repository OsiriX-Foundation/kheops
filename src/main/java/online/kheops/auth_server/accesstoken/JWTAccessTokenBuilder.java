package online.kheops.auth_server.accesstoken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import online.kheops.auth_server.keycloak.KeycloakContextListener;

import javax.servlet.ServletContext;

final class JWTAccessTokenBuilder implements AccessTokenBuilder {
    private static final String HOST_ROOT_PARAMETER = "online.kheops.root.uri";

    private static final String KHEOPS_ISSUER = "auth.kheops.online";
    private static final String SUPERUSER_ISSUER = "authorization.kheops.online";

    private final String issuerHost;

    private final ServletContext servletContext;
    JWTAccessTokenBuilder(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.issuerHost = getIssuerHost();
    }

    @Override
    public AccessToken build(String assertionToken) throws BadAccessTokenException {
        final String issuer;

        try {
            issuer = JWT.decode(assertionToken).getIssuer();
        } catch (JWTDecodeException | NullPointerException e) {
            throw new BadAccessTokenException("Unable to decode JWT", e);
        }

        if (issuer == null) {
            throw new BadAccessTokenException("JWT has no issuer");
        }

        switch (issuer) {
            case KHEOPS_ISSUER:
                return AuthorizationJWTAccessToken.getBuilder(authorizationSecret()).build(assertionToken);
            case SUPERUSER_ISSUER:
                return SuperuserJWTAccessToken.getBuilder(superuserSecret()).build(assertionToken);
            default:
                if (issuer.equals(KeycloakContextListener.getKeycloakIssuer())) {
                    return JWTAccessToken.getBuilder(KeycloakContextListener.getKeycloakOIDCConfigurationString()).build(assertionToken);
                } else if (issuer.equals(issuerHost)) {
                    return ReportProviderAccessToken.getBuilder(servletContext).build(assertionToken);
                } else {
                    throw new BadAccessTokenException("Unknown JWT Issuer:" + issuer);
                }
        }
    }

    private String authorizationSecret() {
        return servletContext.getInitParameter("online.kheops.auth.hmacsecret");
    }

    private String superuserSecret() {
        return servletContext.getInitParameter("online.kheops.superuser.hmacsecret");
    }

    private String getIssuerHost() {
        return servletContext.getInitParameter(HOST_ROOT_PARAMETER);
    }
}
