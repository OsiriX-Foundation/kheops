package online.kheops.auth_server.report_provider.metadata;

import online.kheops.auth_server.report_provider.NoKeyException;

import java.util.Collections;
import java.util.Map;

public interface InnerValueSerialization<T> {

  static <U> Map<String, U> getKeyMap(Class<U> clazz) {
    return Collections.emptyMap();
  }

  static <U> U fromKey(String key, Class<U> clazz) {
    if (getKeyMap(clazz).containsKey(key)) {
      return getKeyMap(clazz).get(key);
    } else {
      throw new IllegalArgumentException("unknown key");
    }
  }

  String getKey() throws NoKeyException;
}
