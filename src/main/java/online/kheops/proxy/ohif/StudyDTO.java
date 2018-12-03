package online.kheops.proxy.ohif;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@XmlRootElement
class StudyDTO {
    @XmlTransient
    private final Map<String, SeriesDTO> seriesMap;

    @XmlElement(name = "series")
    Collection<SeriesDTO> getSeries() {
        return seriesMap.values();
    }

    @XmlElement
    private final String studyInstanceUID;

    @XmlElement
    private final String patientName;

    private StudyDTO() {
        throw new UnsupportedOperationException();
    }

    StudyDTO(final Attributes attributes) {
        seriesMap = new HashMap<>();

        studyInstanceUID = attributes.getString(Tag.StudyInstanceUID);
        patientName = attributes.getString(Tag.PatientName);

        addInstance(attributes);
    }

    void addInstance(final Attributes attributes) {
        seriesMap.computeIfAbsent(attributes.getString(Tag.SeriesInstanceUID), seriesUID -> new SeriesDTO(attributes))
                .addInstance(attributes);
    }

}
