package online.kheops.proxy.ohif;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

@SuppressWarnings("unused")
@XmlRootElement
class MetadataDTO {
    @XmlTransient
    private final Map<String, StudyDTO> studyMap;

    @XmlElement(name = "studies")
    public Collection<StudyDTO> getStudies() {
        return studyMap.values();
    }

    static MetadataDTO from(final List<Attributes> attributesList) {
        final MetadataDTO metadataDTO = new MetadataDTO();
        attributesList.forEach((metadataDTO::addInstance));
        return metadataDTO;
    }

    private MetadataDTO() {
        studyMap = new HashMap<>();
    }

    private void addInstance(final Attributes attributes) {
        studyMap.computeIfAbsent(attributes.getString(Tag.StudyInstanceUID), (studyUID) -> new StudyDTO(attributes)).addInstance(attributes);
    }
}
