package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ResponseType {
    CODE("code"),
    ID_TOKEN("id_token"),
    TOKEN("token");

    private final String key;

    private static final Map<String, ResponseType> KEY_MAP = Collections.unmodifiableMap(keyMap());

    ResponseType(String key) {
        this.key = key;
    }

    public String getKey() throws NoKeyException {
        if (key != null) {
            return key;
        } else {
            throw new NoKeyException();
        }
    }

    public static ResponseType fromKey(String key) {
        if (KEY_MAP.containsKey(key)) {
            return KEY_MAP.get(key);
        } else {
            throw new IllegalArgumentException("unknown key");
        }
    }

    private static Map<String, ResponseType> keyMap() {
        Map<String, ResponseType> map = new HashMap<>();
        for (ResponseType responseType : ResponseType.values()) {
            if (responseType.key != null) {
                map.put(responseType.key, responseType);
            }
        }
        return map;
    }
}
