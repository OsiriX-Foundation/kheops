package online.kheops.auth_server.util;

import javax.servlet.ServletContext;
import java.util.Objects;

public final class TokenBasicAuthenticator {

    private static final String DICOMWEB_PROXY_CLIENT_ID_PARAMETER = "online.kheops.client.dicomwebproxyclientid";
    private static final String DICOMWEB_PROXY_CLIENT_SECRET_PARAMETER = "online.kheops.client.dicomwebproxysecret";
    private static final String ZIPPER_CLIENT_ID_PARAMETER = "online.kheops.client.zipperclientid";
    private static final String ZIPPER_CLIENT_SECRET_PARAMETER = "online.kheops.client.zippersecret";

    private enum KnownClients {
        DICOMWEB_PROXY(DICOMWEB_PROXY_CLIENT_ID_PARAMETER, DICOMWEB_PROXY_CLIENT_SECRET_PARAMETER),
        ZIPPER(ZIPPER_CLIENT_ID_PARAMETER, ZIPPER_CLIENT_SECRET_PARAMETER);

        private String clientIdParameter;
        private String passwordParameter;

        KnownClients(String clientId, String passwordParameter) {
            this.clientIdParameter = clientId;
            this.passwordParameter = passwordParameter;
        }

        public String getClientId(ServletContext context) {
            return context.getInitParameter(clientIdParameter);
        }

        public String getPassword(ServletContext context) {
            return context.getInitParameter(passwordParameter);
        }
    }

    final private ServletContext context;
    private String clientId;
    private String password;

    public static TokenBasicAuthenticator newAuthenticator(final ServletContext context) {
        return new TokenBasicAuthenticator(context);
    }

    private TokenBasicAuthenticator(final ServletContext context) {
        this.context = context;
    }

    public TokenBasicAuthenticator clientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    public TokenBasicAuthenticator password(final String password) {
        this.password = Objects.requireNonNull(password);
        return this;
    }

    public TokenPrincipal authenticate() throws TokenAuthenticationException {
        Objects.requireNonNull(clientId);
        Objects.requireNonNull(password);

        for (KnownClients client: KnownClients.values()) {
            final String knownClient = client.getClientId(context);
            final String knownSecret = client.getPassword(context);
            if (knownClient.equals(clientId) && knownSecret.equals(password)) {
                return new TokenPrincipal() {
                    @Override
                    public TokenClientKind getClientKind() {
                        return TokenClientKind.INTERNAL;
                    }

                    @Override
                    public String getName() {
                        return knownClient;
                    }
                };
            }
        }

        throw new TokenAuthenticationException("unable to authenticate clientId: " + clientId);
    }
}
