package online.kheops.auth_server.token;

import javax.servlet.ServletContext;
import java.util.Objects;

import static online.kheops.auth_server.token.TokenRequestException.Error.INVALID_CLIENT;

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

        public boolean matchesCredentials(ServletContext context, String username, String password) {
            return getClientId(context).equals(username) && getPassword(context).equals(password);
        }
    }

    private final ServletContext context;
    private String clientId;
    private String password;

    static TokenBasicAuthenticator newAuthenticator(final ServletContext context) {
        return new TokenBasicAuthenticator(context);
    }

    private TokenBasicAuthenticator(final ServletContext context) {
        this.context = context;
    }

    public TokenBasicAuthenticator clientId(final String clientId) {
        this.clientId = Objects.requireNonNull(clientId);
        return this;
    }

    TokenBasicAuthenticator password(final String password) {
        this.password = Objects.requireNonNull(password);
        return this;
    }

    TokenPrincipal authenticate() {
        Objects.requireNonNull(clientId);
        Objects.requireNonNull(password);

        for (KnownClients client: KnownClients.values()) {
            if (client.matchesCredentials(context, clientId, password)) {
                return new TokenPrincipal() {
                    @Override
                    public TokenClientKind getClientKind() {
                        return TokenClientKind.INTERNAL;
                    }

                    @Override
                    public String getName() {
                        return clientId;
                    }
                };
            }
        }

        throw new TokenRequestException(INVALID_CLIENT, "unable to authenticate clientId: " + clientId);
    }
}
