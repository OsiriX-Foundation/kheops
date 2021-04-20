package online.kheops.proxy.ohif;

import org.dcm4che3.data.Attributes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.util.*;

@SuppressWarnings("unused")
@XmlRootElement
class MetadataDTO {
    @XmlTransient
    private final URI wadoURI;

    @XmlTransient
    private final String firstSeriesInstanceUID;

    @XmlTransient
    private final SortedMap<UIDKey, StudyDTO> studyMap;

    @XmlElement(name = "studies")
    public Collection<StudyDTO> getStudies() {
        return studyMap.values();
    }

    public static MetadataDTO from(final URI rootURI, final String firstSeriesInstanceUID, final List<Attributes> attributesList) {
        return new MetadataDTO(rootURI, firstSeriesInstanceUID, attributesList);
    }

    private MetadataDTO() {
        throw new UnsupportedOperationException();
    }

    private MetadataDTO(final URI wadoURI, final String firstSeriesInstanceUID, final List<Attributes> attributesList) {
        studyMap = new TreeMap<>();
        this.wadoURI = wadoURI;
        this.firstSeriesInstanceUID = firstSeriesInstanceUID;
        attributesList.forEach(this::addInstance);
    }

    private void addInstance(final Attributes attributes) {
        studyMap.computeIfAbsent(UIDKey.fromStudy(attributes), studyUID -> new StudyDTO(wadoURI, firstSeriesInstanceUID, attributes))
                .addInstance(attributes);
    }
}
