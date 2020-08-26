package online.kheops.auth_server.report_provider;

public class OidcProviderException extends Exception {
  public OidcProviderException() {}

  public OidcProviderException(String message) {
    super(message);
  }

  public OidcProviderException(String message, Throwable cause) {
    super(message, cause);
  }

  public OidcProviderException(Throwable cause) {
    super(cause);
  }

  protected OidcProviderException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
