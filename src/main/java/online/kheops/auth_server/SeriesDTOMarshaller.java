package online.kheops.auth_server;

import org.dcm4che3.json.JSONReader;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Provider
@Consumes("application/dicom+json")
public class SeriesDTOMarshaller implements MessageBodyReader<List<SeriesDTO>> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (aClass.isAssignableFrom(List.class)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                return parameterizedType.getActualTypeArguments()[0] == SeriesDTO.class;
            }
        }
        return false;
    }

    @Override
    public List<SeriesDTO> readFrom(Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream)
            throws WebApplicationException
    {
        List<SeriesDTO> list = new ArrayList<>(1);

        try {
            JsonParser parser = Json.createParser(inputStream);
            JSONReader jsonReader = new JSONReader(parser);

            jsonReader.readDatasets((fmi, dataset) -> {
                SeriesDTO seriesDTO = new SeriesDTO();

                seriesDTO.setModality(dataset.getString(0x00080060));

                list.add(seriesDTO);
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return list;
    }
}
