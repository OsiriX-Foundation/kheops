package online.kheops.auth_server.report_provider.metadata;

import java.util.Map;

public interface ParameterMap extends OidcMetadata {
  Map<Parameter<?>, Object> getMap();

  <T> void put(Parameter<? super T> parameter, T value);

  void putAll(ParameterMap parameterMap);

  <T> T get(Parameter<? extends T> parameter);

  boolean containsKey(Parameter<?> parameter);

  boolean containsValue(Object value);

  int size();

  boolean isEmpty();

  boolean equals(Object var1);

  int hashCode();

  default <T, S extends T, U extends S> T getOrDefault(Parameter<S> parameter, U defaultValue) {
    T v;
    return (v = this.get(parameter)) == null && !this.containsKey(parameter) ? defaultValue : v;
  }

  default <T> T getOrEmptyValue(Parameter<? extends T> parameter) {
    T v;
    return (v = this.get(parameter)) == null && !this.containsKey(parameter) ? parameter.getEmptyValue() : v;
  }
}
