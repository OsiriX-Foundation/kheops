package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ApplicationType {
    WEB("web"),
    NATIVE("native");

    private final String key;

    private static final Map<String, ApplicationType> KEY_MAP = Collections.unmodifiableMap(keyMap());

    ApplicationType(String key) {
        this.key = key;
    }

    public String getKey() throws NoKeyException {
        if (key != null) {
            return key;
        } else {
            throw new NoKeyException();
        }
    }

    public static ApplicationType fromKey(String key) {
        if (KEY_MAP.containsKey(key)) {
            return KEY_MAP.get(key);
        } else {
            throw new IllegalArgumentException("unknown key");
        }
    }

    private static Map<String, ApplicationType> keyMap() {
        Map<String, ApplicationType> map = new HashMap<>();
        for (ApplicationType applicationType : ApplicationType.values()) {
            if (applicationType.key != null) {
                map.put(applicationType.key, applicationType);
            }
        }
        return map;
    }
}
