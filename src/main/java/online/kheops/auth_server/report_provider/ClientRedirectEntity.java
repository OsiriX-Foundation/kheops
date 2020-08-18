package online.kheops.auth_server.report_provider;

import java.net.URI;
import java.util.Optional;

import static online.kheops.auth_server.report_provider.ClientMetadataOptionalStringParameter.CLIENT_NAME;
import static online.kheops.auth_server.report_provider.ClientMetadataURIParameter.INITIATE_LOGIN_URI;

public class ClientRedirectEntity implements LoginRedirectEntity {

    private final ClientMetadata clientMetadata;
    private final String issuer;
    private final String loginHint;

    public ClientRedirectEntity(ClientMetadata clientMetadata, String issuer, String loginHint) {
        this.clientMetadata = clientMetadata;
        this.issuer = issuer;
        this.loginHint = loginHint;
    }

    @Override
    public URI getInitiateLoginUri() {
        return clientMetadata.getValue(INITIATE_LOGIN_URI);
    }

    @Override
    public Optional<String> clientName() {
        return clientMetadata.getValue(CLIENT_NAME);
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public Optional<String> getLoginHint() {
        return Optional.of(loginHint);
    }

    @Override
    public Optional<URI> getTargetLinkUri() {
        return Optional.empty();
    }
}
