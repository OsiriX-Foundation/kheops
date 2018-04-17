package online.kheops.auth_server;

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
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Consumes("application/json")
public class SeriesJSONUnmarshaller implements MessageBodyReader {

    public SeriesJSONUnmarshaller(@Context Providers providers) {
        System.out.println("Building it");
    }
    @Override
    public boolean isReadable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass.isAssignableFrom(JSONSeries.class);
    }

    @Override
    public Object readFrom(Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {

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
