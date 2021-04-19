package online.kheops.proxy.ohif;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.util.*;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@XmlRootElement
class StudyDTO {
    @XmlTransient
    private final URI wadoURI;

    @XmlTransient
    private final SortedMap<UIDKey, SeriesDTO> seriesMap;

    @XmlElement(name = "seriesList")
    Collection<SeriesDTO> getSeries() {
        return seriesMap.values();
    }

    @XmlElement
    private final String studyInstanceUID; // used by OHIF v1

    @XmlElement
    private final String studyInstanceUid; // used by OHIF v2

    @XmlElement
    private final String patientName;

    private StudyDTO() {
        throw new UnsupportedOperationException();
    }

    StudyDTO(final URI wadoURI, final String firstSeriesInstanceUID, final Attributes attributes) {
        this.wadoURI = Objects.requireNonNull(wadoURI);
        Objects.requireNonNull(attributes);

        if (firstSeriesInstanceUID == null) {
            seriesMap = new TreeMap<>();
        } else {
            seriesMap = new TreeMap<>(UIDKey.getFirstUIDComparator(firstSeriesInstanceUID));
        }

        studyInstanceUID = attributes.getString(Tag.StudyInstanceUID);
        studyInstanceUid = attributes.getString(Tag.StudyInstanceUID);
        patientName = attributes.getString(Tag.PatientName);

        addInstance(attributes);
    }

    void addInstance(final Attributes attributes) {
        seriesMap.computeIfAbsent(UIDKey.fromSeries(attributes), seriesUID -> new SeriesDTO(wadoURI, attributes))
                .addInstance(attributes);
    }

}
