package online.kheops.auth_server.series;

import online.kheops.auth_server.entity.Series;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.xml.bind.annotation.XmlElement;

import static online.kheops.auth_server.util.Consts.HOST_ROOT_PARAMETER;

public class SeriesResponse {

    @Context
    private ServletContext context;

    @XmlElement(name = "modality")
    private String modality;
    @XmlElement(name = "series_description")
    private String seriesDescription;
    @XmlElement(name = "series_uid")
    private String seriesUid;
    @XmlElement(name = "number_of_series_related_instance")
    private long numberOfSeriesRelatedInstance;
    @XmlElement(name = "time_zone_offset_from_utc")
    private String timeZoneOffsetFromUTC;
    @XmlElement(name = "series_number")
    private long seriesNumber;
    @XmlElement(name = "body_part_examined")
    private String bodyPartExamined;

    @XmlElement(name = "retrieve_url")
    private String retrieveUrl;

    private SeriesResponse() { /*empty*/ }

    public SeriesResponse(Series series) {
        modality = series.getModality();
        numberOfSeriesRelatedInstance = series.getNumberOfSeriesRelatedInstances();
        seriesDescription = series.getSeriesDescription();
        seriesUid = series.getSeriesInstanceUID();
        timeZoneOffsetFromUTC = series.getTimezoneOffsetFromUTC();
        seriesNumber = series.getSeriesNumber();
        bodyPartExamined = series.getBodyPartExamined();
        retrieveUrl = getHostRoot() + "/api/studies/" + series.getStudy().getStudyInstanceUID() + "/series/" + series.getSeriesInstanceUID();
    }

    private String getHostRoot() {
        return context.getInitParameter(HOST_ROOT_PARAMETER);
    }

}
