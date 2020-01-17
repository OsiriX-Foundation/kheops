package online.kheops.auth_server.series;

import online.kheops.auth_server.entity.Series;

import javax.xml.bind.annotation.XmlElement;

public class SeriesResponse {

    @XmlElement(name = "modality")
    private String modality;
    @XmlElement(name = "series_description")
    private String seriesDescription;
    @XmlElement(name = "series_uid")
    private String seriesUid;
    @XmlElement(name = "number_of_series_related_instance")
    private long numberOfSeriesRelatedInstance;


    private SeriesResponse() { /*empty*/ }

    public SeriesResponse(Series series) {
        this.modality = series.getModality();
        this.numberOfSeriesRelatedInstance = series.getNumberOfSeriesRelatedInstances();
        this.seriesDescription = series.getSeriesDescription();
        this.seriesUid = series.getSeriesInstanceUID();

    }
}
