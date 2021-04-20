package online.kheops.auth_server.report_provider.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParameterHashMap implements ParameterMap {
  private final Map<Parameter<?>, Object> map = new HashMap<>();

  @Override
  public Map<Parameter<?>, Object> getMap() {
    return map;
  }

  @Override
  public <T> void put(Parameter<? super T> parameter, T value) {
    // cast to make sure that a ClassCastException is thrown
    // if the value is of the wrong type.
    map.put(parameter, parameter.cast(value));
  }

  @Override
  public void putAll(ParameterMap parameterMap) {
    map.putAll(parameterMap.getMap());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T get(Parameter<? extends T> parameter) {
    return (T) map.get(parameter);
  }

  @Override
  public boolean containsKey(Parameter<?> parameter) {
    return map.containsKey(parameter);
  }

  @Override
  public <T> T getValue(Parameter<? extends T> parameter) {
    final T value = get(parameter);
    if (value != null) {
      return value;
    } else {
      return parameter.getEmptyValue();
    }
  }

  @Override
  public <T> T getValue(Parameter<? extends T> parameter, List<Locale.LanguageRange> priorityList) {
    return getValue(parameter);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ParameterMap) {
      return getMap().equals(((ParameterMap) obj).getMap());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getMap().hashCode();
  }
}
