package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.metadata.Parameter;

import javax.json.JsonValue;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

public interface OptionalParameter<T> extends Parameter<Optional<T>> {
    default Optional<T> getEmptyValue() {
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    default Optional<T> cast(Object value) {
        if (value instanceof Optional) {
            Optional<?> optional = (Optional<?>) value;

            Class<?> declaringClass = ((Enum<?>) this).getDeclaringClass();
            Class<?> genericClass = (Class<?>) ((ParameterizedType) declaringClass.getGenericInterfaces()[0]).getActualTypeArguments()[0];

            optional.ifPresent(optionalValue -> {
                if (!genericClass.isAssignableFrom(optionalValue.getClass())) {
                    throw new ClassCastException("Optional with value with " + optionalValue.getClass()
                        + " can not be assigned to " + name());
                }
            });
            return (Optional<T>) value;
        } else {
            throw new ClassCastException("object of " + value.getClass()
                    + " can not be assigned to parameter " + name());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    default Class<Optional<T>> getValueType() {
        return (Class<Optional<T>>) Optional.empty().getClass();
    }

    T innerValueFrom(JsonValue jsonValue);

    JsonValue jsonFromInnerValue(T value);

    @Override
    default Optional<T> valueFrom(JsonValue jsonValue) {
        return Optional.ofNullable(innerValueFrom(jsonValue));
    }

    @Override
    default JsonValue jsonFrom(Optional<T> value) {
        return jsonFromInnerValue(value.orElseThrow(() -> new IllegalArgumentException("Empty Optional")));
    }
}
