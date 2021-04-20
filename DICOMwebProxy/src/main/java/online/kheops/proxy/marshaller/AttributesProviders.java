package online.kheops.proxy.marshaller;

import org.dcm4che3.data.Attributes;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

final class AttributesProviders {

    private AttributesProviders() {
        throw new AssertionError();
    }

    static boolean isAttributeList(Class<?> aClass, Type genericType) {
        if (List.class.isAssignableFrom(aClass) && genericType instanceof ParameterizedType) {
            Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            if (listType instanceof Class) {
                return Attributes.class.isAssignableFrom((Class<?>)listType);
            }
        }
        return false;
    }
}
