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

    @XmlElement(name = "studyInstanceUID")
    private final String studyInstanceUidV1;

    @XmlElement(name = "studyInstanceUid")
    private final String studyInstanceUidV2;

    @XmlElement
    private final String patientName;

    private StudyDTO() {
        throw new UnsupportedOperationException();
    }

    StudyDTO(final URI wadoURI, final OHIFVersion ohifVersion, final String firstSeriesInstanceUID, final Attributes attributes) {
        this.wadoURI = Objects.requireNonNull(wadoURI);
        Objects.requireNonNull(attributes);

        if (firstSeriesInstanceUID == null) {
            seriesMap = new TreeMap<>();
        } else {
            seriesMap = new TreeMap<>(UIDKey.getFirstUIDComparator(firstSeriesInstanceUID));
        }

        switch (ohifVersion) {
            case V1:
                studyInstanceUidV1 = attributes.getString(Tag.StudyInstanceUID);
                studyInstanceUidV2 = null;
                break;
            case V2:
                studyInstanceUidV2 = attributes.getString(Tag.StudyInstanceUID);
                studyInstanceUidV1 = null;
                break;
            default:
                throw new IllegalArgumentException("Unknown OHIF Version");
        }
        patientName = attributes.getString(Tag.PatientName);

        addInstance(attributes);
    }

    void addInstance(final Attributes attributes) {
        seriesMap.computeIfAbsent(UIDKey.fromSeries(attributes), seriesUID -> new SeriesDTO(wadoURI, attributes))
                .addInstance(attributes);
    }

}
