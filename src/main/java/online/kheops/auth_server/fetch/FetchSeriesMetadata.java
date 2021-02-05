package online.kheops.auth_server.fetch;

public class FetchSeriesMetadata {

    private Integer numberOfNewInstances;

    public FetchSeriesMetadata(Integer numberOfNewInstances) {
        this.numberOfNewInstances = numberOfNewInstances;
    }

    public Integer getNumberOfNewInstances() { return numberOfNewInstances; }
}
