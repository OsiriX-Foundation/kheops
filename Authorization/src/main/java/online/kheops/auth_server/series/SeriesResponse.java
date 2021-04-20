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
    @XmlElement(name = "number_of_series_related_instances")
    private Long numberOfSeriesRelatedInstance;
    @XmlElement(name = "time_zone_offset_from_utc")
    private String timeZoneOffsetFromUTC;
    @XmlElement(name = "series_number")
    private Long seriesNumber;
    @XmlElement(name = "body_part_examined")
    private String bodyPartExamined;
    @XmlElement(name = "retrieve_url")
    private String retrieveUrl;

    @XmlElement(name = "number_of_new_instances")
    private Integer numberOfNewInstances;


    private SeriesResponse() { /*empty*/ }

    public void hideRetrieveUrl() {
        retrieveUrl = null;
    }

    public static class Builder {

        private Series series;
        private boolean showRetrieveUrl;
        private Integer numberOfNewInstances;
        private boolean uidOnly;
        private String kheopsInstance;

        public Builder(Series series) {
            this.series = series;
            showRetrieveUrl = false;
            uidOnly = false;
        }

        public Builder showRetrieveUrl() {
            this.showRetrieveUrl = true;
            return this;
        }

        public Builder showRetrieveUrl(boolean showRetrieveUrl) {
            this.showRetrieveUrl = showRetrieveUrl;
            return this;
        }

        public Builder hideRetrieveUrl() {
            this.showRetrieveUrl = false;
            return this;
        }

        public Builder kheopsInstance(String kheopsInstance) {
            this.kheopsInstance = kheopsInstance;
            return this;
        }

        public Builder uidOnly(boolean uidOnly) {
            this.uidOnly = uidOnly;
            return this;
        }

        public Builder numberOfNewInstances(Integer numberOfNewInstances) {
            this.numberOfNewInstances = numberOfNewInstances;
            return this;
        }

        public SeriesResponse build() {
            final SeriesResponse seriesResponse = new SeriesResponse();
            seriesResponse.seriesUid = series.getSeriesInstanceUID();
            if (!uidOnly) {
                seriesResponse.modality = series.getModality();
                seriesResponse.numberOfSeriesRelatedInstance = Long.valueOf(series.getNumberOfSeriesRelatedInstances());
                seriesResponse.seriesDescription = series.getSeriesDescription();
                seriesResponse.timeZoneOffsetFromUTC = series.getTimezoneOffsetFromUTC();
                seriesResponse.seriesNumber = Long.valueOf(series.getSeriesNumber());
                seriesResponse.bodyPartExamined = series.getBodyPartExamined();
                if (numberOfNewInstances != null) {
                    seriesResponse.numberOfNewInstances = numberOfNewInstances;
                }
            }
            if (kheopsInstance != null && showRetrieveUrl) {
                seriesResponse.retrieveUrl = kheopsInstance + "/api/studies/" + series.getStudy().getStudyInstanceUID() + "/series/" + series.getSeriesInstanceUID();
            }

            return seriesResponse;
        }
    }
}