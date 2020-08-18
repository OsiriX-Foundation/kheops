package online.kheops.auth_server.report_provider;

import java.net.URI;
import java.util.Optional;

public interface LoginRedirectEntity {
    URI getInitiateLoginUri();
    String getIssuer();
    Optional<String> clientName();
    Optional<String> getLoginHint();
    Optional<URI> getTargetLinkUri();
}
