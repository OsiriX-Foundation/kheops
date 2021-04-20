package online.kheops.auth_server.marshaller;

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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static org.glassfish.jersey.message.filtering.spi.FilteringHelper.EMPTY_ANNOTATIONS;

@Provider
@Produces("multipart/related;type=\"application/dicom+xml\"")
public class XMLAttributesListWriter implements MessageBodyWriter<List<Attributes>> {

    private final Providers providers;

    public XMLAttributesListWriter(@Context final Providers providers) {
        this.providers = providers;
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
    public void writeTo(List<Attributes> attributesList, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
        throws IOException {

        MultiPart multiPart = new MultiPart(new MediaType("multipart", "related", Collections.singletonMap("type", "application/dicom+xml")));
        for (Attributes attributes: attributesList) {
            multiPart.bodyPart(attributes, new MediaType("application", "dicom+xml"));
        }

        final MessageBodyWriter<MultiPart> bodyWriter = providers.getMessageBodyWriter(
                MultiPart.class,
                MultiPart.class,
                EMPTY_ANNOTATIONS,
                new MediaType("multipart", "related"));

        if (bodyWriter == null) {
            throw new IllegalArgumentException();
        }

        bodyWriter.writeTo(
                multiPart,
                MultiPart.class,
                MultiPart.class,
                EMPTY_ANNOTATIONS,
                multiPart.getMediaType(),
                httpHeaders,
                entityStream
        );
    }
}
