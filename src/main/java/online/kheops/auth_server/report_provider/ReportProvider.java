package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.report_provider.metadata.OidcMetadata;

import java.util.Optional;

public class ReportProvider {

  private final OidcMetadata clientMetadata;
  private final String albumIdRestriction;

  public ReportProvider(OidcMetadata clientMetadata, String albumIdRestriction) {
    this.clientMetadata = clientMetadata;
    this.albumIdRestriction = albumIdRestriction;
  }

  public Optional<String> getAlbumIdRestriction() {
    return Optional.ofNullable(albumIdRestriction);
  }

  public OidcMetadata getClientMetadata() {
    return clientMetadata;
  }
}
