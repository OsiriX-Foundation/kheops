package online.kheops.proxy.multipart;

import org.glassfish.jersey.media.multipart.internal.LocalizationMessages;
import org.glassfish.jersey.message.MessageUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.glassfish.jersey.media.multipart.Boundary.BOUNDARY_PARAMETER;

final class MultipartWriterImpl implements MultipartWriter {

    private final OutputStream outputStream;
    private final Providers providers;
    private final Annotation[] annotations;
    private final MediaType mediaType;
    private final Writer writer;

    private boolean firstPartWritten = false;

    MultipartWriterImpl(final OutputStream outputStream,
                        final MediaType mediaType,
                        final Providers providers,
                        final Annotation[] annotations) {
        this.outputStream = outputStream;
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, MessageUtils.getCharset(mediaType)));
        this.mediaType = mediaType;
        this.providers = providers;
        this.annotations = annotations;
    }

    @Override
    public void writePart(final StreamingBodyPart bodyPart) throws IOException {
        writeLeadingBoundary();

        // Write the headers for this body part
        final MediaType bodyMediaType = bodyPart.getMediaType();
        if (bodyMediaType == null) {
            throw new IllegalArgumentException(LocalizationMessages.MISSING_MEDIA_TYPE_OF_BODY_PART());
        }

        final MultivaluedMap<String, Object> bodyHeaders = bodyPart.getHeaders();
        bodyHeaders.putSingle(CONTENT_TYPE, bodyMediaType.toString());

        if (bodyHeaders.getFirst(CONTENT_DISPOSITION) == null && bodyPart.getContentDisposition() != null) {
            bodyHeaders.putSingle(CONTENT_DISPOSITION, bodyPart.getContentDisposition().toString());
        }

        writeHeaders(bodyHeaders);

        // Mark the end of the headers for this body part
        writer.write("\r\n");
        writer.flush();

        // Write the entity for this body part
        final Object bodyEntity = bodyPart.getEntity();
        if (bodyEntity == null) {
            throw new IllegalArgumentException(LocalizationMessages.MISSING_ENTITY_OF_BODY_PART(bodyMediaType));
        }

        @SuppressWarnings("unchecked")
        Class<Object> bodyClass = (Class<Object>) bodyEntity.getClass();

        final MessageBodyWriter<Object> bodyWriter = providers.getMessageBodyWriter(
                bodyClass,
                bodyClass,
                annotations,
                bodyMediaType);

        if (bodyWriter == null) {
            throw new IllegalArgumentException(LocalizationMessages.NO_AVAILABLE_MBW(bodyClass, mediaType));
        }

        bodyWriter.writeTo(
                bodyEntity,
                bodyClass,
                bodyClass,
                annotations,
                bodyMediaType,
                bodyHeaders,
                outputStream
        );
        outputStream.flush();
    }

    private String getBoundary() {
        return mediaType.getParameters().get(BOUNDARY_PARAMETER);
    }

    private void writeLeadingBoundary() throws IOException {
        if (!firstPartWritten) {
            firstPartWritten = true;
            writer.write("--");
        } else {
            writer.write("\r\n--");
        }
        writer.write(getBoundary());
        writer.write("\r\n");
        writer.flush();
    }

    private void writeHeaders(final MultivaluedMap<String, Object> headers) throws IOException {
        for (final Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            // Write this header and its value(s)
            writer.write(entry.getKey());
            writer.write(':');
            boolean first = true;
            for (final Object object : entry.getValue()) {
                final String value = object.toString();
                if (first) {
                    writer.write(' ');
                    first = false;
                } else {
                    writer.write(',');
                }
                writer.write(value);
            }
            writer.write("\r\n");
        }
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
