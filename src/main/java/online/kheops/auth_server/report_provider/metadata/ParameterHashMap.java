package online.kheops.auth_server.report_provider.metadata;

import java.util.HashMap;
import java.util.Map;

public class ParameterHashMap implements ParameterMap {
  private final Map<Parameter<?>, Object> map = new HashMap<>();

  @Override
  public Map<Parameter<?>, Object> getMap() {
    return map;
  }

  @Override
  public <T> void put(Parameter<T> parameter, T value) {
    map.put(parameter, value);
  }

  @Override
  public void putAll(ParameterMap parameterMap) {
    map.putAll(parameterMap.getMap());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(Parameter<T> parameter) {
    return (T) map.get(parameter);
  }

  @Override
  public boolean containsKey(Parameter<?> parameter) {
    return map.containsKey(parameter);
  }
}
