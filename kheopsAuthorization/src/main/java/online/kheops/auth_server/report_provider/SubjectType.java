package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum SubjectType {
  PAIRWISE("pairwise"),
  PUBLIC("public");

  private final String key;

  private static final Map<String, SubjectType> KEY_MAP = Collections.unmodifiableMap(keyMap());

  SubjectType(String key) {
    this.key = key;
  }

  public String getKey() throws NoKeyException {
    if (key != null) {
      return key;
    } else {
      throw new NoKeyException();
    }
  }

  public static SubjectType fromKey(String key) {
    if (KEY_MAP.containsKey(key)) {
      return KEY_MAP.get(key);
    } else {
      throw new IllegalArgumentException("unknown key");
    }
  }

  private static Map<String, SubjectType> keyMap() {
    Map<String, SubjectType> map = new HashMap<>();
    for (SubjectType subjectType : SubjectType.values()) {
      if (subjectType.key != null) {
        map.put(subjectType.key, subjectType);
      }
    }
    return map;
  }

  }
