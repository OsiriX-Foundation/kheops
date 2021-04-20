package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Encoding {
    A128CBC_HS256("A128CBC_HS256");

    private final String key;

    private static final Map<String, Encoding> KEY_MAP = Collections.unmodifiableMap(keyMap());

    Encoding(String key) {
        this.key = key;
    }

    public String getKey() throws NoKeyException {
        if (key != null) {
            return key;
        } else {
            throw new NoKeyException();
        }
    }

    public static Encoding fromKey(String key) {
        if (KEY_MAP.containsKey(key)) {
            return KEY_MAP.get(key);
        } else {
            throw new IllegalArgumentException("unknown key");
        }
    }

    private static Map<String, Encoding> keyMap() {
        Map<String, Encoding> map = new HashMap<>();
        for (Encoding encoding : Encoding.values()) {
            if (encoding.key != null) {
                map.put(encoding.key, encoding);
            }
        }

        return map;
    }
}
