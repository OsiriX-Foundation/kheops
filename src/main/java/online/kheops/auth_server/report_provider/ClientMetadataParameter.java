package online.kheops.auth_server.report_provider;

import java.lang.reflect.ParameterizedType;

public interface ClientMetadataParameter<T> {
    String getKey();
    String name();
    T getEmptyValue();

    default boolean isLocalizable() {
        return false;
    }

    @SuppressWarnings("unchecked")
    default T cast(Object value) {
        if (!getValueType().isAssignableFrom(value.getClass())) {
            throw new ClassCastException("object with " + value.getClass()
                    + " can not be assigned to parameter " + name());
        }
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    default Class<T> getValueType() {
        Class<? extends ClientMetadataParameter<T>> declaringClass = ((Enum<? extends ClientMetadataParameter<T>>) this).getDeclaringClass();
        return (Class<T>) ((ParameterizedType) declaringClass.getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }
}
