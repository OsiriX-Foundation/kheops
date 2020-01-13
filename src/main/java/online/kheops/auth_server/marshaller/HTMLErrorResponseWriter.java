package online.kheops.auth_server.marshaller;

import online.kheops.auth_server.util.ErrorResponse;
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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(MediaType.TEXT_HTML)
public class HTMLErrorResponseWriter implements MessageBodyWriter<ErrorResponse> {

    private static final String part1 =
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <title>title</title>\n" +
            "    <link rel=\"stylesheet\" href=\"style.css\">\n" +
            "    <script src=\"script.js\"></script>\n" +
            "  </head>\n" +
            "  <body>\n";

    private static final String part2 =
            "  </body>\n" +
            "</html>";

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(ErrorResponse.class);
    }

    @Override
    public void writeTo(ErrorResponse errorResponse, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException
    {

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(part1);
        stringBuilder.append(errorResponse.toString());
        stringBuilder.append(part2);

        DataOutputStream dataOutputStream = new DataOutputStream(entityStream);
        dataOutputStream.writeUTF(stringBuilder.toString());

    }
}
