package online.kheops.auth_server.report_provider;

import java.util.Optional;

public class ReportProvider {

    private final ClientMetadata clientMetadata;
    private final String albumIdRestriction;

    public ReportProvider(ClientMetadata clientMetadata, String albumIdRestriction) {
        this.clientMetadata = clientMetadata;
        this.albumIdRestriction = albumIdRestriction;
    }

    public Optional<String> getAlbumIdRestriction() {
        return Optional.ofNullable(albumIdRestriction);
    }

    public ClientMetadata getClientMetadata() {
        return clientMetadata;
    }
}
