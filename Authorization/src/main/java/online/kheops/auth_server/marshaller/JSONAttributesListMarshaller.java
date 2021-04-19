package online.kheops.auth_server.marshaller;

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
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Provider
@Consumes("application/dicom+json,application/json,text/plain")
@Produces("application/dicom+json,application/json")
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
    public List<Attributes> readFrom(Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream)
            throws IOException {
        final List<Attributes> list = new ArrayList<>();

        if(mediaType.isCompatible(MediaType.TEXT_PLAIN_TYPE)) {
            if(inputStream.read() != -1) {
                throw new IOException("Expected empty inputstream");
            }
        } else {

            try (final JsonParser parser = Json.createParser(new FilterInputStream(inputStream) {
                @Override
                public void close() {/* close shield */}
            })) {
                final JSONReader jsonReader = new JSONReader(parser);
                jsonReader.readDatasets((fmi, dataset) -> list.add(dataset));
            } catch (Exception e) {
                throw new WebApplicationException("Error while reading JSON datasets", e);
            }

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

        try (final JsonGenerator generator = Json.createGenerator(new FilterOutputStream(entityStream) { @Override public void close() {/* close shield */} })) {
            final JSONWriter jsonWriter = new JSONWriter(generator);

            generator.writeStartArray();

            for (final Attributes attributes : attributesList) {
                jsonWriter.write(attributes);
            }

            generator.writeEnd();
            generator.flush();
        }
    }
}
