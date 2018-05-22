package online.kheops.auth_server;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Provider
@Consumes("application/dicom+json")
@Produces("application/dicom+json")
public class AttributesListMarshaller implements MessageBodyReader<List<Attributes>>, MessageBodyWriter<List<Attributes>> {

    private static final Logger LOG = LoggerFactory.getLogger(AttributesListMarshaller.class);

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (aClass.isAssignableFrom(List.class)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                return parameterizedType.getActualTypeArguments()[0] == Attributes.class;
            }
        }
        return false;
    }

    @Override
    public List<Attributes> readFrom(Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream)
            throws WebApplicationException {
        List<Attributes> list = new ArrayList<>();

        try {
            JsonParser parser = Json.createParser(inputStream);
            JSONReader jsonReader = new JSONReader(parser);
            jsonReader.readDatasets((fmi, dataset) -> list.add(dataset));
        } catch (Throwable t) {
            LOG.error("Error while parsing JSON dataset", t);
        }

        return list;
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (List.class.isAssignableFrom(aClass)) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                return parameterizedType.getActualTypeArguments()[0] == Attributes.class;
            }
        }
        return false;
    }

    @Override
    public void writeTo(List<Attributes> attributesList, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws javax.ws.rs.WebApplicationException {

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
