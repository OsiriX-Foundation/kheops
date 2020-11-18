package online.kheops.auth_server.stow;

public class Level4_InstancesLevel {
    private boolean isNewSeries;
    private boolean isNewInDestination;
    private Integer numberOfInstances;


    public Level4_InstancesLevel(boolean isNewSeries, Integer numberOfInstances, boolean isNewInDestination) {
        this.isNewSeries = isNewSeries;
        this.isNewInDestination = isNewInDestination;
        this.numberOfInstances = numberOfInstances;
    }

    public boolean isNewSeries() { return isNewSeries; }
    public boolean isNewInDestination() { return isNewInDestination; }
    public Integer getNumberOfInstances() { return numberOfInstances; }

    @Override
    public String toString() {
        String s = "\n\t\t\t\t{";
        s += "is_new_series:" + isNewSeries;
        s += "\n\t\t\t\tis_new_in_destination:" + isNewInDestination;
        s += "\n\t\t\t\tnumber_of_instances:" + numberOfInstances;
        return s;
    }
}