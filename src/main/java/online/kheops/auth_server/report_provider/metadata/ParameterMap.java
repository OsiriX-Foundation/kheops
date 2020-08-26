package online.kheops.auth_server.report_provider.metadata;

import java.util.Map;

public interface ParameterMap {
  Map<Parameter<?>, Object> getMap();

  <T> void put(Parameter<T> parameter, T value);

  void putAll(ParameterMap parameterMap);

  <T> T get(Parameter<T> parameter);

  boolean containsKey(Parameter<?> parameter);
}
