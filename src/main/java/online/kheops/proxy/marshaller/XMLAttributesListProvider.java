package online.kheops.proxy.marshaller;

import org.dcm4che3.data.Attributes;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import static org.dcm4che3.ws.rs.MediaTypes.*;

@Provider
@Produces("multipart/related;type=\"application/dicom+xml\"")
public class XMLAttributesListProvider implements MessageBodyWriter<List<Attributes>> {

    private final Providers providers;

    public XMLAttributesListProvider(@Context final Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return AttributesProviders.isAttributeList(aClass, genericType);
    }

    @Override
    public void writeTo(List<Attributes> attributesList, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException {

        MultiPart multiPart = new MultiPart(MULTIPART_RELATED_APPLICATION_DICOM_XML_TYPE);
        for (Attributes attributes: attributesList) {
            multiPart.bodyPart(attributes, APPLICATION_DICOM_XML_TYPE);
        }

        final MessageBodyWriter<MultiPart> bodyWriter = providers.getMessageBodyWriter(
                MultiPart.class,
                MultiPart.class,
                annotations,
                MULTIPART_RELATED_TYPE);

        if (bodyWriter == null) {
            throw new IllegalArgumentException();
        }

        bodyWriter.writeTo(
                multiPart,
                MultiPart.class,
                MultiPart.class,
                annotations,
                multiPart.getMediaType(),
                httpHeaders,
                entityStream
        );
    }
}
