package online.kheops.auth_server.report_provider.metadata.parameters;

import online.kheops.auth_server.report_provider.metadata.Parameter;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

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
            Class<?> genericClass = (Class<?>) ((ParameterizedType) declaringClass.getGenericInterfaces()[0]).getActualTypeArguments()[0];

            list.forEach(listValue -> {
                if (!genericClass.isAssignableFrom(listValue.getClass())) {
                    throw new ClassCastException("object in List with " + listValue.getClass()
                            + " can not be assigned to " + name());
                }
            });
            return (List<T>) value;
        } else {
            throw new ClassCastException("object of class " + value.getClass()
                    + " can not be assigned to parameter " + name());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    default Class<List<T>> getValueType() {
        return (Class<List<T>>) (Class<?>) List.class;
    }
}
