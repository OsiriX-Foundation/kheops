package online.kheops.auth_server.report_provider;

import java.util.*;

public enum Algorithm  {
  NONE("none"),
  ANY_SUPPORTED(null),
  UNDECLARED(null),
  RS256("RS256");

  private final String key;

  private static final Map<String, Algorithm> KEY_MAP = Collections.unmodifiableMap(keyMap());

  Algorithm(String key) {
    this.key = key;
  }

  public String getKey() throws NoKeyException {
    if (key != null) {
      return key;
    } else {
      throw new NoKeyException();
    }
  }

  public static Algorithm fromKey(String key) {
    if (KEY_MAP.containsKey(key)) {
      return KEY_MAP.get(key);
    } else {
      throw new IllegalArgumentException("unknown key");
    }
  }

  private static Map<String, Algorithm> keyMap() {
    Map<String, Algorithm> map = new HashMap<>();
    for (Algorithm algorithm : Algorithm.values()) {
      if (algorithm.key != null) {
        map.put(algorithm.key, algorithm);
      }
    }
    return map;
  }
}
