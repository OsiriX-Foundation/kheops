package online.kheops.proxy.marshaller;

import org.dcm4che3.data.Attributes;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

abstract class AttributesListMarshaller {

    protected boolean isAttributeList(Class<?> aClass, Type genericType) {
        if (List.class.isAssignableFrom(aClass) && genericType instanceof ParameterizedType) {
            Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            if (listType instanceof Class) {
                return Attributes.class.isAssignableFrom((Class<?>)listType);
            }
        }
        return false;
    }
}
