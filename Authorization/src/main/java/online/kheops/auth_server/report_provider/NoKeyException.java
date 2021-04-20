package online.kheops.auth_server.report_provider;

public class NoKeyException extends Exception {
  public NoKeyException() {
  }

  public NoKeyException(String message) {
    super(message);
  }

  public NoKeyException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoKeyException(Throwable cause) {
    super(cause);
  }

  protected NoKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
