package online.kheops.proxy.marshaller;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_JSON;

@Provider
@Consumes(APPLICATION_DICOM_JSON + "," + APPLICATION_JSON)
@Produces(APPLICATION_DICOM_JSON + "," + APPLICATION_JSON)
public class JSONAttributesListMarshaller implements MessageBodyReader<List<Attributes>>, MessageBodyWriter<List<Attributes>> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (aClass.isAssignableFrom(List.class) && type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parameterizedType.getActualTypeArguments()[0] == Attributes.class;
        }
        return false;
    }

    @Override
    public List<Attributes> readFrom(Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream) {
        List<Attributes> list = new ArrayList<>();

        try {
            JsonParser parser = Json.createParser(inputStream);
            JSONReader jsonReader = new JSONReader(parser);
            jsonReader.readDatasets((fmi, dataset) -> list.add(dataset));
        } catch (Exception e){
            throw new WebApplicationException("Error while reading JSON datasets", e);
        }

        return list;
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (List.class.isAssignableFrom(aClass) && genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            return parameterizedType.getActualTypeArguments()[0] == Attributes.class;
        }
        return false;
    }

    @Override
    public void writeTo(List<Attributes> attributesList, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {

        JsonGenerator generator = Json.createGenerator(entityStream);
        JSONWriter jsonWriter = new JSONWriter(generator);

        generator.writeStartArray();

        for (Attributes attributes: attributesList) {
            jsonWriter.write(attributes);
        }

        generator.writeEnd();
        generator.flush();
    }
}
