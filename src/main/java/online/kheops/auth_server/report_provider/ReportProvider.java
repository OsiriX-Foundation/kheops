package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.token.TokenClientKind;
import online.kheops.auth_server.token.TokenPrincipal;

import javax.security.auth.Subject;
import java.util.Optional;

import static online.kheops.auth_server.report_provider.ClientMetadataOptionalStringParameter.CLIENT_NAME;

public class ReportProvider {

    private final ClientMetadata clientMetadata;
    private final String albumID;

    public ReportProvider(ClientMetadata clientMetadata, String albumID) {
        this.clientMetadata = clientMetadata;
        this.albumID = albumID;
    }

    public Optional<String> getAlbumID() {
        return Optional.ofNullable(albumID);
    }

    public ClientMetadata getClientMetadata() {
        return clientMetadata;
    }
}
