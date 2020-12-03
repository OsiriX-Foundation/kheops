package online.kheops.auth_server.fetch;

public class FetchSeriesMetadata {

    private boolean isNewSeries;
    private boolean isNewStudy;
    private Integer numberOfNewInstances;

    public FetchSeriesMetadata(boolean isNewSeries, boolean isNewStudy, Integer numberOfNewInstances) {
        this.isNewSeries = isNewSeries;
        this.isNewStudy = isNewStudy;
        this.numberOfNewInstances = numberOfNewInstances;
    }

    public boolean isNewSeries() { return isNewSeries; }
    public boolean isNewStudy() { return isNewStudy; }
    public Integer getNumberOfNewInstances() { return numberOfNewInstances; }
}
