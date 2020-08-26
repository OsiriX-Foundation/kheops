package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.report_provider.metadata.OidcMetadata;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalStringParameter.CLIENT_NAME;
import static online.kheops.auth_server.report_provider.metadata.parameters.OptionalUriBuilderParameter.KHEOPS_TARGET_LINK_URI_TEMPLATE;
import static online.kheops.auth_server.report_provider.metadata.parameters.URIParameter.INITIATE_LOGIN_URI;

public class ClientRedirectEntity implements LoginRedirectEntity {

    private static final Logger LOG = Logger.getLogger(ClientRedirectEntity.class.getName());

    private final OidcMetadata clientMetadata;
    private final String issuer;
    private final String studyInstanceUid;
    private final String loginHint;

    public ClientRedirectEntity(OidcMetadata clientMetadata, String issuer, String studyInstanceUid, String loginHint) {
        this.clientMetadata = clientMetadata;
        this.issuer = issuer;
        this.loginHint = loginHint;
        this.studyInstanceUid = studyInstanceUid;
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
        UriBuilder uriBuilder = clientMetadata.getValue(KHEOPS_TARGET_LINK_URI_TEMPLATE).orElse(null);
        if (uriBuilder != null) {
            try {
                return Optional.of(uriBuilder.buildFromMap(Collections.singletonMap("StudyInstanceUID", studyInstanceUid)));
            } catch (IllegalArgumentException | UriBuilderException e) {
                LOG.log(WARNING, "Unable to build target_link_uri", e);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
