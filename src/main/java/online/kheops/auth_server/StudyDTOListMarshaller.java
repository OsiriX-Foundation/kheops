package online.kheops.auth_server;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.json.JSONReader;
import org.dcm4che3.json.JSONWriter;
import org.dcm4che3.util.StringUtils;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Provider
@Consumes("application/dicom+json")
@Produces("application/dicom+json")
public class StudyDTOListMarshaller implements MessageBodyReader<List<StudyDTO>>, MessageBodyWriter<List<StudyDTO>> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        if (aClass.isAssignableFrom(List.class)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                return parameterizedType.getActualTypeArguments()[0] == StudyDTO.class;
            }
        }
        return false;
    }

    @Override
    public List<StudyDTO> readFrom(Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, InputStream inputStream)
            throws WebApplicationException
    {
        List<StudyDTO> list = new ArrayList<>(1);

        try {
            JsonParser parser = Json.createParser(inputStream);
            JSONReader jsonReader = new JSONReader(parser);

            jsonReader.readDatasets((fmi, dataset) -> {
                StudyDTO studyDTO = new StudyDTO();

                studyDTO.setStudyDate(dataset.getString(Tag.StudyDate));
                studyDTO.setStudyTime(dataset.getString(Tag.StudyTime));
                studyDTO.setTimezoneOffsetFromUTC(dataset.getString(Tag.TimezoneOffsetFromUTC));
                studyDTO.setAccessionNumber(dataset.getString(Tag.AccessionNumber));
                studyDTO.setReferringPhysicianName(dataset.getString(Tag.ReferringPhysicianName));
                studyDTO.setPatientName(dataset.getString(Tag.PatientName));
                studyDTO.setPatientID(dataset.getString(Tag.PatientID));
                studyDTO.setPatientBirthDate(dataset.getString(Tag.PatientBirthDate));
                studyDTO.setPatientSex(dataset.getString(Tag.PatientSex));
                studyDTO.setStudyID(dataset.getString(Tag.StudyID));

                list.add(studyDTO);
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (List.class.isAssignableFrom(aClass)) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    return parameterizedType.getActualTypeArguments()[0] == StudyDTO.class;
            }
        }
        return false;
    }

    @Override
    public void writeTo(List<StudyDTO> studyDTOList, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws java.io.IOException, javax.ws.rs.WebApplicationException {

        JsonGenerator generator = Json.createGenerator(entityStream);
        JSONWriter jsonWriter = new JSONWriter(generator);

        generator.writeStartArray();

        for (StudyDTO studyDTO: studyDTOList) {
            Attributes attributes = new Attributes();
            safeAttributeSetString(attributes, Tag.StudyDate, VR.DA, studyDTO.getStudyDate());
            safeAttributeSetString(attributes, Tag.StudyTime, VR.TM, studyDTO.getStudyTime());
            safeAttributeSetString(attributes, Tag.TimezoneOffsetFromUTC, VR.SH, studyDTO.getTimezoneOffsetFromUTC());
            safeAttributeSetString(attributes, Tag.AccessionNumber, VR.SH, studyDTO.getAccessionNumber());
            safeAttributeSetString(attributes, Tag.ModalitiesInStudy, VR.CS, StringUtils.concat(studyDTO.getModalities(), '\\'));
            safeAttributeSetString(attributes, Tag.ReferringPhysicianName, VR.PN, studyDTO.getReferringPhysicianName());
            safeAttributeSetString(attributes, Tag.PatientName, VR.PN, studyDTO.getPatientName());
            safeAttributeSetString(attributes, Tag.PatientID, VR.LO, studyDTO.getPatientID());
            safeAttributeSetString(attributes, Tag.PatientBirthDate, VR.DA, studyDTO.getPatientBirthDate());
            safeAttributeSetString(attributes, Tag.PatientSex, VR.CS, studyDTO.getPatientSex());
            safeAttributeSetString(attributes, Tag.StudyInstanceUID, VR.UI, studyDTO.getStudyInstanceUID());
            safeAttributeSetString(attributes, Tag.StudyID, VR.SH, studyDTO.getStudyID());
            safeAttributeSetString(attributes, Tag.InstanceAvailability, VR.CS, "ONLINE");
            attributes.setInt(Tag.NumberOfStudyRelatedSeries, VR.IS, studyDTO.getSeries().size());
            attributes.setInt(Tag.NumberOfStudyRelatedInstances, VR.IS, studyDTO.getNumberOfInstances());

            jsonWriter.write(attributes);
        }

        generator.writeEnd();
        generator.flush();
    }

    private void safeAttributeSetString(Attributes attributes, int tag, VR vr, String string) {
        if (string != null) {
            attributes.setString(tag, vr, string);
        }
    }

}
