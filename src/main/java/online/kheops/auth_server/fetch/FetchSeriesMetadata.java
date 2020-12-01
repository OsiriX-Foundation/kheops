package online.kheops.auth_server.fetch;

public class FetchSeriesMetadata {

    private boolean isNewSeries;
    private boolean isNewStudy;
    private Integer oldNumberOfSeriesRelatedInstances;

    public FetchSeriesMetadata(boolean isNewSeries, boolean isNewStudy, Integer oldNumberOfSeriesRelatedInstances) {
        this.isNewSeries = isNewSeries;
        this.isNewStudy = isNewStudy;
        this.oldNumberOfSeriesRelatedInstances = oldNumberOfSeriesRelatedInstances;
    }

    public boolean isNewSeries() { return isNewSeries; }
    public boolean isNewStudy() { return isNewStudy; }
    public Integer getOldNumberOfSeriesRelatedInstances() { return oldNumberOfSeriesRelatedInstances; }
}
