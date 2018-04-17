package online.kheops.auth_server;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.xml.sax.SAXException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReaderFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Provider
@Consumes("multipart/related")
public class SeriesXMLUnmarshaller implements MessageBodyReader {
    public SeriesXMLUnmarshaller() {
        System.out.println("Building it");
    }

    public SeriesXMLUnmarshaller(@Context Providers providers) {
        System.out.println("Building it");
    }
    @Override
    public boolean isReadable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass.isAssignableFrom(JSONSeries.class);
    }

    @Override
    public Object readFrom(Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {

        Attributes attrs = new Attributes();
        ContentHandlerAdapter ch = new ContentHandlerAdapter(attrs);
        SAXParserFactory f = SAXParserFactory.newInstance();
        SAXParser p = null;
        try {
            p = f.newSAXParser();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        try {
            p.parse(inputStream, ch);
        } catch (SAXException e) {
            e.printStackTrace();
        }

        Map<String, Object> properties = attrs.getProperties();
        System.out.println(properties);


        Object result = null;
        try {
            JsonObject json;

            JsonArray array = Json.createReaderFactory(null).createReader(inputStream).readArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
