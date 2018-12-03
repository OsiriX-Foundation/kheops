package online.kheops.proxy.ohif;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;

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

    static InstanceDTO from(Attributes attributes) {
        return new InstanceDTO(attributes.getString(Tag.SOPInstanceUID),
                attributes.getInt(Tag.Columns, 0),
                attributes.getInt(Tag.Rows, 0),
                getURL(attributes));
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

    private static String getURL(Attributes attributes) {
        return UriBuilder.fromUri("dicomweb://test2.kheops.online/wado?studyUID={studyUID}&seriesUID={seriesUID}&objectUID={objectUID}&requestType=WADO&contentType=application/dicom")
                .resolveTemplate("studyUID", attributes.getString(Tag.StudyInstanceUID))
                .resolveTemplate("seriesUID", attributes.getString(Tag.SeriesInstanceUID))
                .resolveTemplate("objectUID", attributes.getString(Tag.SOPInstanceUID))
                .build().toString();
    }
}
