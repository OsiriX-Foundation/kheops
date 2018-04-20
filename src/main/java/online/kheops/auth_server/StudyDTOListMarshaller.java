package online.kheops.auth_server;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class StudyDTOListMarshaller implements MessageBodyWriter<List<StudyDTO>> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (aClass.isAssignableFrom(List.class)) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    return parameterizedType.getActualTypeArguments()[0] == StudyDTO.class;
            }
        }
        return false;
    }

    @Override
    public void writeTo(List<StudyDTO> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws java.io.IOException, javax.ws.rs.WebApplicationException {

        JsonGenerator generator = Json.createGenerator(entityStream);
        JSONWriter jsonWriter = new JSONWriter(generator);

        Attributes attributes = new Attributes();

//        String modalitiesInStudy = "MR"
    }

}
