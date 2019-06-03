package online.kheops.auth_server.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import online.kheops.auth_server.keycloak.KeycloakContextListener;

import javax.servlet.ServletContext;

final class JWTAssertionBuilder implements AssertionBuilder {
    private static final String HOST_ROOT_PARAMETER = "online.kheops.root.uri";

    private static final String KHEOPS_ISSUER = "auth.kheops.online";
    private static final String SUPERUSER_ISSUER = "authorization.kheops.online";
    private static final String GOOGLE_ISSUER = "accounts.google.com";

    private static final String GOOGLE_CONFIGURATION_URL = "https://accounts.google.com/.well-known/openid-configuration";

    private final String issuerHost;

    private final ServletContext servletContext;
    JWTAssertionBuilder(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.issuerHost = getIssuerHost();
    }

    @Override
    public Assertion build(String assertionToken) throws BadAssertionException {
        final String issuer;

        try {
            issuer = JWT.decode(assertionToken).getIssuer();
        } catch (JWTDecodeException | NullPointerException e) {
            throw new BadAssertionException("Unable to decode JWT", e);
        }

        if (issuer == null) {
            throw new BadAssertionException("JWT has no issuer");
        }

        switch (issuer) {
            case KHEOPS_ISSUER:
                return AuthorizationJWTAssertion.getBuilder(authorizationSecret()).build(assertionToken);
            case SUPERUSER_ISSUER:
                return SuperuserJWTAssertion.getBuilder(superuserSecret()).build(assertionToken);
            case GOOGLE_ISSUER:
                return JWTAssertion.getBuilder(GOOGLE_CONFIGURATION_URL).build(assertionToken);
            default:
                if (issuer.equals(KeycloakContextListener.getKeycloakIssuer())) {
                    return JWTAssertion.getBuilder(KeycloakContextListener.getKeycloakOIDCConfigurationString()).build(assertionToken);
                } else if (issuer.equals(issuerHost)) {
                    return ReportProviderAssertion.getBuilder(servletContext).build(assertionToken);
                } else {
                    throw new BadAssertionException("Unknown JWT Issuer:" + issuer);
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
