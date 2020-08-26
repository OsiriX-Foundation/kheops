package online.kheops.auth_server.report_provider;

import online.kheops.auth_server.report_provider.metadata.parameters.StringParameter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public final class ReportProviderCatalogueImp implements ReportProviderCatalogue {
  private final OhifClientMetadata OHIF_CLIENT_METADATA = new OhifClientMetadata(ohifURI());

  private static URI ohifURI() {
    try {
      return new URI("http://127.0.0.1:3000");
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public ReportProvider getReportProvider(String clientID) throws ReportProviderNotFoundException {
    Objects.requireNonNull(clientID);

    if (clientID.equals(OHIF_CLIENT_METADATA.getValue(StringParameter.CLIENT_ID))) {
      return new ReportProvider(OHIF_CLIENT_METADATA, null);
    } else {
      throw new ReportProviderNotFoundException(
          "ClientID does not match any known report providers");
    }
  }
}
