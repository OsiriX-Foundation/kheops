package online.kheops.proxy.marshaller;

import online.kheops.proxy.multipart.MultipartStreamingOutput;
import org.apache.commons.io.output.CloseShieldOutputStream;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.dcm4che3.ws.rs.MediaTypes.*;
import static org.glassfish.jersey.message.filtering.spi.FilteringHelper.EMPTY_ANNOTATIONS;

@Provider
@Produces(APPLICATION_DICOM_JSON + "," + APPLICATION_JSON + "," + "multipart/related;type=\"application/dicom+xml\"")
public class StreamingAttributesMessageBodyWriter implements MessageBodyWriter<AttributesStreamingOutput> {

    private final Providers providers;

    public StreamingAttributesMessageBodyWriter(@Context final Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return AttributesStreamingOutput.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(AttributesStreamingOutput attributesStreamingOutput, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream)
            throws IOException, WebApplicationException {

        if (mediaType.isCompatible(APPLICATION_DICOM_JSON_TYPE) || mediaType.isCompatible(APPLICATION_JSON_TYPE)) {
            try (JSONStreamingAttributesWriter attributesWriter = new JSONStreamingAttributesWriter(new CloseShieldOutputStream(entityStream))) {
                attributesWriter.writeStart();
                attributesStreamingOutput.write(attributesWriter);
                attributesWriter.writeEnd();
                attributesWriter.flush();
            }
        } else if (mediaType.isCompatible(MULTIPART_RELATED_APPLICATION_DICOM_XML_TYPE)) {

            final MessageBodyWriter<MultipartStreamingOutput> bodyWriter = providers.getMessageBodyWriter(
                    MultipartStreamingOutput.class,
                    MultipartStreamingOutput.class,
                    EMPTY_ANNOTATIONS,
                    MULTIPART_RELATED_APPLICATION_DICOM_XML_TYPE);

            if (bodyWriter == null) {
                throw new IllegalArgumentException();
            }

            MultipartStreamingOutput output = multipartOutputStream -> {
                StreamingAttributesWriter writer = new XMLStreamingAttributesWriter(multipartOutputStream);
                attributesStreamingOutput.write(writer);
            };

            bodyWriter.writeTo(output,
                    MultipartStreamingOutput.class,
                    MultipartStreamingOutput.class,
                    EMPTY_ANNOTATIONS,
                    MULTIPART_RELATED_APPLICATION_DICOM_XML_TYPE,
                    httpHeaders,
                    entityStream);
        } else {
            throw new IOException("Unknown media type");
        }
    }
}
