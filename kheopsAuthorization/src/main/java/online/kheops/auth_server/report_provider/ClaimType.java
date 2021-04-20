package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ClaimType {
  NORMAL("normal"),
  AGGREGATED("aggregated"),
  DISTRIBUTED("distributed");

  private final String key;

  private static final Map<String, ClaimType> KEY_MAP = Collections.unmodifiableMap(keyMap());

  ClaimType(String key) {
    this.key = key;
  }

  public String getKey() throws NoKeyException {
    if (key != null) {
      return key;
    } else {
      throw new NoKeyException();
    }
  }

  public static ClaimType fromKey(String key) {
    if (KEY_MAP.containsKey(key)) {
      return KEY_MAP.get(key);
    } else {
      throw new IllegalArgumentException("unknown key");
    }
  }

  private static Map<String, ClaimType> keyMap() {
    Map<String, ClaimType> map = new HashMap<>();
    for (ClaimType claimType : ClaimType.values()) {
      if (claimType.key != null) {
        map.put(claimType.key, claimType);
      }
    }
    return map;
  }
}
