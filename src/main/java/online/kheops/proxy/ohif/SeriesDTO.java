package online.kheops.proxy.ohif;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
class SeriesDTO {
    @XmlTransient
    private final Map<String, InstanceDTO> instanceMap;

    @XmlElement(name = "instances")
    Collection<InstanceDTO> getInstances() {
        return instanceMap.values();
    }

    @XmlElement
    private final String seriesInstanceUid;

    @XmlElement
    private final String seriesDescription;

    private SeriesDTO() {
        throw new UnsupportedOperationException();
    }

    SeriesDTO(final Attributes attributes) {
        instanceMap = new HashMap<>();

        seriesInstanceUid = attributes.getString(Tag.SeriesInstanceUID);
        seriesDescription = attributes.getString(Tag.SeriesDescription);

        addInstance(attributes);
    }

    void addInstance(final Attributes attributes) {
        instanceMap.computeIfAbsent(attributes.getString(Tag.SOPInstanceUID), (seriesUID) -> InstanceDTO.from(attributes));
    }
}
