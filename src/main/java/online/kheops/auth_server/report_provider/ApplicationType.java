package online.kheops.auth_server.report_provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum ApplicationType {
    WEB("web"),
    NATIVE("native");

    private final String string;

    private static final Map<String, ApplicationType> STRING_MAP = Collections.unmodifiableMap(stringMap());

    ApplicationType(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

    public static ApplicationType fromString(String string) {
        if (STRING_MAP.containsKey(string)) {
            return STRING_MAP.get(string);
        } else {
            throw new IllegalArgumentException("unknown string value");
        }
    }

    private static Map<String, ApplicationType> stringMap() {
        Map<String, ApplicationType> map = new HashMap<>();
        for (ApplicationType applicationType : ApplicationType.values()) {
            if (applicationType.string != null) {
                map.put(applicationType.string, applicationType);
            }
        }
        return map;
    }
}
