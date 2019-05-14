package online.kheops.proxy.marshaller;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_JSON;

@Provider
@Consumes(APPLICATION_DICOM_JSON + "," + APPLICATION_JSON)
@Produces(APPLICATION_DICOM_JSON + "," + APPLICATION_JSON)
public class JSONAttributesListMarshaller implements MessageBodyReader<List>, MessageBodyWriter<List> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (aClass.isAssignableFrom(List.class) && type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parameterizedType.getActualTypeArguments()[0] == Attributes.class;
        }
        return false;
    }

    @Override
    public List<Attributes> readFrom(Class<List> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException {
        List<Attributes> list = new ArrayList<>();

        try (final JsonParser parser = Json.createParser(new CloseShieldInputStream(inputStream))) {
            final JSONReader jsonReader = new JSONReader(parser);
            jsonReader.readDatasets((fmi, dataset) -> list.add(dataset));
        } catch (JsonParsingException e) {
            final JsonLocation jsonLocation = e.getLocation();
            throw new IOException("Error while reading JSON datasets, (line:" + jsonLocation.getLineNumber() +
                    ", column:" + jsonLocation.getColumnNumber() +
                    ", stream offset:" + jsonLocation.getStreamOffset() + ")", e);
        } catch (Exception e){
            throw new IOException("Error while reading JSON datasets", e);
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
    public void writeTo(List attributesList, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {

        try (final JsonGenerator generator = Json.createGenerator(new CloseShieldOutputStream(entityStream))) {
            final JSONWriter jsonWriter = new JSONWriter(generator);

            generator.writeStartArray();

            for (final Object object : attributesList) {
                if (object instanceof Attributes) {
                    jsonWriter.write((Attributes) object);
                } else {
                    throw new IllegalArgumentException("Trying to write an object that is not of class Attributes");
                }
            }

            generator.writeEnd();
            generator.flush();
        }
    }
}
