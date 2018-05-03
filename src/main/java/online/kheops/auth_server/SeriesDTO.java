package online.kheops.auth_server;

import online.kheops.auth_server.entity.Series;

public class SeriesDTO {

    private String modality;
    private String timezoneOffsetFromUTC;
    private String seriesDescription;
    private String seriesInstanceUID;
    private int seriesNumber;
    private int numberOfSeriesRelatedInstances;

    public SeriesDTO() {}

    public SeriesDTO(Series series) {
        this.modality = series.getModality();
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getTimezoneOffsetFromUTC() {
        return timezoneOffsetFromUTC;
    }

    public void setTimezoneOffsetFromUTC(String timezoneOffsetFromUTC) {
        this.timezoneOffsetFromUTC = timezoneOffsetFromUTC;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID) {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public int getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(int seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public int getNumberOfSeriesRelatedInstances() {
        return numberOfSeriesRelatedInstances;
    }

    public void setNumberOfSeriesRelatedInstances(int numberOfSeriesRelatedInstances) {
        this.numberOfSeriesRelatedInstances = numberOfSeriesRelatedInstances;
    }
}
