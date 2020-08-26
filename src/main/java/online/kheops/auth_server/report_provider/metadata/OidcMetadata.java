package online.kheops.auth_server.report_provider.metadata;

import java.util.Locale;

public interface OidcMetadata {
  <T> T getValue(Parameter<? extends T> parameter);

  <T> T getValue(Parameter<? extends T> parameter, Locale local);
}
