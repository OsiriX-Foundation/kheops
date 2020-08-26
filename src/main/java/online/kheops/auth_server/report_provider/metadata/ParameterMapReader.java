package online.kheops.auth_server.report_provider.metadata;

import javax.json.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

import static javax.xml.bind.JAXBIntrospector.getValue;
import static online.kheops.auth_server.report_provider.metadata.parameters.ListStringParameter.CONTACTS;

public class ParameterMapReader implements MessageBodyReader<ParameterMap> {
  @Override
  public boolean isReadable(
      Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return type.isAssignableFrom(ParameterMap.class);
  }

  @Override
  public ParameterMap readFrom(
      Class<ParameterMap> type,
      Type genericType,
      Annotation[] annotations,
      MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream)
      throws IOException, WebApplicationException {

    final ParameterHashMap parameterMap = new ParameterHashMap();

    try (InputStreamReader inputStreamReader =
            new InputStreamReader(
                new FilterInputStream(entityStream) {@Override public void close() {}});
        JsonReader jsonReader = Json.createReader(inputStreamReader)) {

      JsonObject jsonObject = jsonReader.readObject();

      for (String key: jsonObject.keySet()) {
        @SuppressWarnings("unchecked") Parameter<Object> parameter = (Parameter<Object>) Parameters.parameterFromKey(key);
        parameterMap.put(parameter, parameter.valueFrom(jsonObject.getJsonObject(key)));
      }
    } catch (JsonException | IllegalArgumentException e) {
      throw new IOException("Unable to parse JSON", e);
    }

    return parameterMap;
  }
}
