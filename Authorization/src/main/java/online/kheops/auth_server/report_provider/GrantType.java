package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum GrantType {
    AUTHORIZATION_CODE("authorization_code"),
    IMPLICIT("implicit"),
    REFRESH_TOKEN("refresh_token");

    private final String key;

    private static final Map<String, GrantType> KEY_MAP = Collections.unmodifiableMap(keyMap());

    GrantType(String key) {
        this.key = key;
    }

    public String getKey() throws NoKeyException {
        if (key != null) {
            return key;
        } else {
            throw new NoKeyException();
        }
    }

    public static GrantType fromKey(String key) {
        if (KEY_MAP.containsKey(key)) {
            return KEY_MAP.get(key);
        } else {
            throw new IllegalArgumentException("unknown key");
        }
    }

    private static Map<String, GrantType> keyMap() {
        Map<String, GrantType> map = new HashMap<>();
        for (GrantType grantType : GrantType.values()) {
            if (grantType.key != null) {
                map.put(grantType.key, grantType);
            }
        }
        return map;
    }
}
