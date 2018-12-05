package online.kheops.proxy.ohif;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.util.*;

@SuppressWarnings("unused")
@XmlRootElement
class MetadataDTO {
    @XmlTransient
    private final URI rootURI;

    @XmlTransient
    private final Map<String, StudyDTO> studyMap;

    @XmlElement(name = "studies")
    public Collection<StudyDTO> getStudies() {
        return studyMap.values();
    }

    public static MetadataDTO from(final URI rootURI, final List<Attributes> attributesList) {
        return new MetadataDTO(rootURI, attributesList);
    }

    private MetadataDTO() {
        throw new UnsupportedOperationException();
    }

    private MetadataDTO(final URI rootURI, final List<Attributes> attributesList) {
        studyMap = new HashMap<>();
        this.rootURI = rootURI;
        attributesList.forEach(this::addInstance);
    }

    private void addInstance(final Attributes attributes) {
        studyMap.computeIfAbsent(attributes.getString(Tag.StudyInstanceUID), studyUID -> new StudyDTO(rootURI, attributes))
                .addInstance(attributes);
    }
}
