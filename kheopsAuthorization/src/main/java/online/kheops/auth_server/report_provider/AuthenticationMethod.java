package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum AuthenticationMethod {
  CLIENT_SECRET_BASIC("client_secret_basic"),
  PRIVATE_KEY_JWT("private_key_jwt");

  private final String key;

  private static final Map<String, AuthenticationMethod> KEY_MAP =
      Collections.unmodifiableMap(keyMap());

  AuthenticationMethod(String key) {
    this.key = key;
  }

  public String getKey() throws NoKeyException {
    if (key != null) {
      return key;
    } else {
      throw new NoKeyException();
    }
  }

  public static AuthenticationMethod fromKey(String key) {
    if (KEY_MAP.containsKey(key)) {
      return KEY_MAP.get(key);
    } else {
      throw new IllegalArgumentException("unknown key");
    }
  }

  private static Map<String, AuthenticationMethod> keyMap() {
    Map<String, AuthenticationMethod> map = new HashMap<>();
    for (AuthenticationMethod authenticationMethod : AuthenticationMethod.values()) {
      if (authenticationMethod.key != null) {
        map.put(authenticationMethod.key, authenticationMethod);
      }
    }
    return map;
  }
}
