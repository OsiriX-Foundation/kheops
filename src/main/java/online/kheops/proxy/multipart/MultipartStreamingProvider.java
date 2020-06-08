package online.kheops.proxy.multipart;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.glassfish.jersey.media.multipart.Boundary;
import org.glassfish.jersey.message.MessageUtils;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces("multipart/*")
public class MultipartStreamingProvider implements MessageBodyWriter<MultipartStreamingOutput> {

    private final Providers providers;

    public MultipartStreamingProvider(@Context final Providers providers) {
        this.providers = providers;
    }

    @Override
    public long getSize(final MultipartStreamingOutput multipartStreamingOutput,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(final Class<?> type,
                               final Type genericType,
                               final Annotation[] annotations,
                               final MediaType mediaType) {
        return MultipartStreamingOutput.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(final MultipartStreamingOutput multipartStreamingOutput,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> headers,
                        final OutputStream stream) throws IOException {

        final Object value = headers.getFirst("MIME-Version");
        if (value == null) {
            headers.putSingle("MIME-Version", "1.0");
        }

        // Determine the boundary string to be used, creating one if needed.
        final MediaType boundaryMediaType = Boundary.addBoundary(mediaType);
        if (boundaryMediaType != mediaType) {
            headers.putSingle(HttpHeaders.CONTENT_TYPE, boundaryMediaType.toString());
        }

        // Build the MultiPartOutputStream
        try (final MultipartWriter multipartWriter = new MultipartWriterImpl(
                new CloseShieldOutputStream(stream),
                boundaryMediaType,
                providers,
                annotations)) {
            multipartStreamingOutput.write(multipartWriter);
            multipartWriter.flush();
        }

        // Write the final boundary string
        try (final Writer writer = new BufferedWriter(
                new OutputStreamWriter(new CloseShieldOutputStream(stream), MessageUtils.getCharset(mediaType)),
                512)) {
            writer.write("\r\n--");
            writer.write(boundaryMediaType.getParameters().get("boundary"));
            writer.write("--\r\n");
            writer.flush();
        }
    }
}
