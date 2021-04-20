package online.kheops.auth_server.report_provider.metadata;

import online.kheops.auth_server.filter.SecuredFilter;

import javax.json.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public class ParameterMapReader implements MessageBodyReader<ParameterMap> {

  private static final Logger LOG = Logger.getLogger(SecuredFilter.class.getName());

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
                new FilterInputStream(entityStream) {
                  @Override
                  public void close() {}
                });
         final JsonReader jsonReader = Json.createReader(inputStreamReader))
    {
      final JsonObject jsonObject = jsonReader.readObject();

      for (String key : jsonObject.keySet()) {
        writeKey(key, jsonObject, parameterMap);
      }
    } catch (JsonException e) {
      throw new IOException("Unable to parse JSON", e);
    }

    return parameterMap;
  }

  private static void writeKey(String key, JsonObject jsonObject, ParameterMap parameterMap) {
    @SuppressWarnings("unchecked")
    Parameter<Object> parameter = (Parameter<Object>) Parameters.parameterFromKey(key);
    if (parameter == null) {
      LOG.log(WARNING, () -> "parameter key: " + key + " is unknown, skipping");
      return;
    }
    try {
      parameterMap.put(parameter, parameter.valueFrom(jsonObject.get(key)));
    } catch (JsonException | IllegalArgumentException e) {
      LOG.log(WARNING, "unable to read parameter key: " + key + " is unknown, skipping", e);
    }
  }
}
