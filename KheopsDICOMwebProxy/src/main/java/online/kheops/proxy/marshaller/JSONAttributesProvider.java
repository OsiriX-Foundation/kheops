package online.kheops.proxy.marshaller;

import org.apache.commons.io.output.CloseShieldOutputStream;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_JSON;

@Provider
@Produces({APPLICATION_DICOM_JSON,APPLICATION_JSON})
public class JSONAttributesProvider implements MessageBodyWriter<Attributes> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Attributes.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(Attributes attributes, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        try (final JsonGenerator generator = Json.createGenerator(new CloseShieldOutputStream(entityStream))) {
            JSONWriter jsonWriter = new JSONWriter(generator);
            jsonWriter.write(attributes);
            generator.flush();
        }
    }
}
