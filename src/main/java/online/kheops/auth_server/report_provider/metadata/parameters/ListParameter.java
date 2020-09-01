package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.fetch.Fetcher;
import online.kheops.auth_server.report_provider.metadata.Parameter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonValue;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.logging.Level.WARNING;

public interface ListParameter<T> extends Parameter<List<T>> {

  default List<T> getEmptyValue() {
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  @Override
  default List<T> cast(Object value) {
    if (value instanceof List) {
      List<?> list = (List<?>) value;

      Class<?> declaringClass = ((Enum<?>) this).getDeclaringClass();
      Class<?> genericClass =
          (Class<?>)
              ((ParameterizedType) declaringClass.getGenericInterfaces()[0])
                  .getActualTypeArguments()[0];

      list.forEach(
          listValue -> {
            if (!genericClass.isAssignableFrom(listValue.getClass())) {
              throw new ClassCastException(
                  "object in List with "
                      + listValue.getClass()
                      + " can not be assigned to "
                      + name());
            }
          });
      return (List<T>) value;
    } else {
      throw new ClassCastException(
          "object of class " + value.getClass() + " can not be assigned to parameter " + name());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  default Class<List<T>> getValueType() {
    return (Class<List<T>>) (Class<?>) List.class;
  }

  T innerValueFrom(JsonValue jsonValue);

  JsonValue jsonFromInnerValue(T value);

  @Override
  default List<T> valueFrom(JsonValue jsonValue) {
    if (jsonValue instanceof JsonArray) {
      final List<T> list = new ArrayList<>();
      for (JsonValue value: jsonValue.asJsonArray()) {
        try {
          list.add(innerValueFrom(value));
        } catch (IllegalArgumentException | JsonException e) {
          Logger.getLogger(ListParameter.class.getName()).log(WARNING, "unknown value: " + value.toString(), e);
        }
      }
      return list;
    } else {
      throw new IllegalArgumentException("Not an array");
    }
  }

  @Override
  default JsonValue jsonFrom(List<T> value) {
    return Json.createArrayBuilder(
            value.stream().map(this::jsonFromInnerValue).collect(Collectors.toList()))
        .build();
  }
}
