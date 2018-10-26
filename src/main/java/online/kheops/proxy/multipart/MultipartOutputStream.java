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

public class MultipartOutputStream extends FilterOutputStream {
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    private final Providers providers;
    private final MediaType mediaType;
    private final Writer writer;

    private boolean firstPartWritten = false;

    MultipartOutputStream(OutputStream out, MediaType mediaType, final Providers providers) {
        super(out);
        this.mediaType = mediaType;
        this.providers = providers;
        writer = new BufferedWriter(new OutputStreamWriter(out, MessageUtils.getCharset(mediaType)));

    }

    public void writePart(StreamingBodyPart bodyPart) throws IOException {
        writeLeadingBoundary();

        // Write the headers for this body part
        final MediaType bodyMediaType = bodyPart.getMediaType();
        if (bodyMediaType == null) {
            throw new IllegalArgumentException(LocalizationMessages.MISSING_MEDIA_TYPE_OF_BODY_PART());
        }

        final MultivaluedMap<String, String> bodyHeaders = bodyPart.getHeaders();
        bodyHeaders.putSingle("Content-Type", bodyMediaType.toString());

        if (bodyHeaders.getFirst("Content-Disposition") == null && bodyPart.getContentDisposition() != null) {
            bodyHeaders.putSingle("Content-Disposition", bodyPart.getContentDisposition().toString());
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

        Class bodyClass = bodyEntity.getClass();

        //noinspection unchecked
        final MessageBodyWriter bodyWriter = providers.getMessageBodyWriter(
                bodyClass,
                bodyClass,
                EMPTY_ANNOTATIONS,
                bodyMediaType);

        if (bodyWriter == null) {
            throw new IllegalArgumentException(LocalizationMessages.NO_AVAILABLE_MBW(bodyClass, mediaType));
        }

        //noinspection unchecked
        bodyWriter.writeTo(
                bodyEntity,
                bodyClass,
                bodyClass,
                EMPTY_ANNOTATIONS,
                bodyMediaType,
                bodyHeaders,
                this
        );
        flush();
    }

    private String getBoundary() {
        return mediaType.getParameters().get("boundary");
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
    }

    private void writeHeaders(final MultivaluedMap<String, String> headers) throws IOException {
        for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
            // Write this header and its value(s)
            writer.write(entry.getKey());
            writer.write(':');
            boolean first = true;
            for (final String value : entry.getValue()) {
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
    }
}
