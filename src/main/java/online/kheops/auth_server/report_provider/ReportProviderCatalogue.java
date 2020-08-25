package online.kheops.auth_server.report_provider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public final class ReportProviderCatalogue {
  private final OhifClientMetadata OHIF_CLIENT_METADATA = new OhifClientMetadata(ohifURI());

  private static URI ohifURI() {
    try {
      return new URI("http://127.0.0.1:3000");
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  public ReportProvider getReportProvider(String clientID) throws ReportProviderNotFoundException {
    Objects.requireNonNull(clientID);

    if (clientID.equals(OHIF_CLIENT_METADATA.getValue(ClientMetadataStringParameter.CLIENT_ID))) {
      return new ReportProvider(OHIF_CLIENT_METADATA, null);
    } else {
      throw new ReportProviderNotFoundException(
          "ClientID does not match any known report providers");
    }
  }
}
