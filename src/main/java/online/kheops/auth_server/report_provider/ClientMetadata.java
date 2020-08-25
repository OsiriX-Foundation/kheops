package online.kheops.auth_server.report_provider;

import java.util.Locale;

public interface ClientMetadata {
  <T> T getValue(ClientMetadataParameter<T> parameter);

  <T> T getValue(ClientMetadataParameter<T> parameter, Locale local);
}
