package online.kheops.proxy.marshaller;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.SAXTransformer;
import org.xml.sax.SAXException;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.dcm4che3.ws.rs.MediaTypes.APPLICATION_DICOM_XML;


@Provider
@Produces(APPLICATION_DICOM_XML)
public class XMLAttributesProvider implements MessageBodyWriter<Attributes> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(Attributes.class);
    }

    @Override
    public void writeTo(Attributes attributes, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        try {
            SAXTransformer.getSAXWriter(new StreamResult(entityStream)).write(attributes);
        } catch (TransformerConfigurationException | SAXException e) {
            throw new IOException("Error with transformer", e);
        }
    }
}
