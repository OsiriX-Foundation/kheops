package online.kheops.auth_server.stow;

public class Level2Value {

    private boolean newSeries;
    private boolean newInDestination;
    private Integer oldNumberOfSeriesRelatedInstance;
    private boolean sendUpload;

    public Level2Value(boolean isNewSeries, Integer numberOfInstances, boolean isNewInDestination, boolean isSend) {
        this.newSeries = isNewSeries;
        this.newInDestination = isNewInDestination;
        this.oldNumberOfSeriesRelatedInstance = numberOfInstances;
        this.sendUpload = isSend;
    }

    public boolean isNewSeries() { return newSeries; }

    public boolean isNewInDestination() { return newInDestination; }

    public Integer getOldNumberOfSeriesRelatedInstance() { return oldNumberOfSeriesRelatedInstance; }

    public boolean isSendUpload() { return sendUpload; }

    public void add(boolean isNewSeries, Integer numberOfInstances, boolean isNewInDestination, boolean isSend) {
        //this.newSeries = isNewSeries;
        //this.newInDestination = isNewInDestination;
        //this.oldNumberOfSeriesRelatedInstance = numberOfInstances;
        this.sendUpload = isSend;
    }
}
