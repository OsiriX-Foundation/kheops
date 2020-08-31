package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum DisplayType {
  PAGE("page"),
  POPUP("popup"),
  TOUCH("touch"),
  WAP("wap");

  private final String key;

  private static final Map<String, DisplayType> KEY_MAP = Collections.unmodifiableMap(keyMap());

  DisplayType(String key) {
    this.key = key;
  }

  public String getKey() throws NoKeyException {
    if (key != null) {
      return key;
    } else {
      throw new NoKeyException();
    }
  }

  public static DisplayType fromKey(String key) {
    if (KEY_MAP.containsKey(key)) {
      return KEY_MAP.get(key);
    } else {
      throw new IllegalArgumentException("unknown key");
    }
  }

  private static Map<String, DisplayType> keyMap() {
    Map<String, DisplayType> map = new HashMap<>();
    for (DisplayType displayType : DisplayType.values()) {
      if (displayType.key != null) {
        map.put(displayType.key, displayType);
      }
    }
    return map;
  }

}
