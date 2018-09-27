package online.kheops.proxy;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.json.JSONWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces("application/dicom+json")
public class JSONAttributesWriter implements MessageBodyWriter<Attributes> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(Attributes.class);
    }

    @Override
    public void writeTo(Attributes attributes, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        JsonGenerator generator = Json.createGenerator(entityStream);
        JSONWriter jsonWriter = new JSONWriter(generator);
        jsonWriter.write(attributes);
        generator.flush();
    }
}
