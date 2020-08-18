package online.kheops.auth_server.report_provider;

import java.util.HashMap;
import java.util.Map;

public class ClientMetadataParameterMap {
    private final Map<ClientMetadataParameter<?>, Object> map = new HashMap<>();

    private Map<ClientMetadataParameter<?>, Object> getMap() {
        return map;
    }

    public <T> void put(ClientMetadataParameter<T> parameter, T value) {
        map.put(parameter, value);
    }

    public void putAll(ClientMetadataParameterMap parameterMap) {
        map.putAll(parameterMap.getMap());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ClientMetadataParameter<T> parameter) {
        return (T)map.get(parameter);
    }

    public boolean containsKey(ClientMetadataParameter<?> parameter) {
        return map.containsKey(parameter);
    }
}
