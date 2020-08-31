package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ResponseMode {
  QUERY("query"),
  FRAGMENT("fragment"),
  FORM_POST("form_post"),
  WEB_MESSAGE("web_message");

  private final String key;

  private static final Map<String, ResponseMode> KEY_MAP = Collections.unmodifiableMap(keyMap());

  ResponseMode(String key) {
    this.key = key;
  }

  public String getKey() throws NoKeyException {
    if (key != null) {
      return key;
    } else {
      throw new NoKeyException();
    }
  }

  public static ResponseMode fromKey(String key) {
    if (KEY_MAP.containsKey(key)) {
      return KEY_MAP.get(key);
    } else {
      throw new IllegalArgumentException("unknown key");
    }
  }

  private static Map<String, ResponseMode> keyMap() {
    Map<String, ResponseMode> map = new HashMap<>();
    for (ResponseMode responseMode : ResponseMode.values()) {
      if (responseMode.key != null) {
        map.put(responseMode.key, responseMode);
      }
    }
    return map;
  }
}
