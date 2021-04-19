package online.kheops.proxy.ohif;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
class InstanceDTO {
    @XmlElement
    private final String sopInstanceUid;

    @XmlElement
    private final int columns;

    @XmlElement
    private final int rows;

    @XmlElement
    private final String url;

    static InstanceDTO from(final URI wadoURI, Attributes attributes) {
        return new InstanceDTO(attributes.getString(Tag.SOPInstanceUID),
                attributes.getInt(Tag.Columns, 0),
                attributes.getInt(Tag.Rows, 0),
                getURL(wadoURI, attributes));
    }

    private InstanceDTO() {
        throw new UnsupportedOperationException();
    }

    private InstanceDTO(final String sopInstanceUid, final int columns, final int rows, final String url) {
        this.sopInstanceUid = sopInstanceUid;
        this.columns = columns;
        this.rows = rows;
        this.url = url;
    }

    private static String getURL(final URI rootURI, Attributes attributes) {
        return "wadouri:" + UriBuilder.fromUri(rootURI)
                .queryParam("studyUID", attributes.getString(Tag.StudyInstanceUID))
                .queryParam("seriesUID", attributes.getString(Tag.SeriesInstanceUID))
                .queryParam("objectUID", attributes.getString(Tag.SOPInstanceUID))
                .queryParam("requestType", "WADO")
                .queryParam("contentType", "application/dicom")
                .build().toString();
    }
}
