package online.kheops.auth_server.stow;

public class Level4_InstancesLevel {
    private boolean isNewSeries;
    private boolean isNewInDestination;


    public Level4_InstancesLevel(boolean isNewSeries, boolean isNewInDestination) {
        this.isNewSeries = isNewSeries;
        this.isNewInDestination = isNewInDestination;
    }

    public boolean isNewSeries() { return isNewSeries; }
    public boolean isNewInDestination() { return isNewInDestination; }

    @Override
    public String toString() {
        String s = "\n\t\t\t\t{";
        s += "is_new_series:" + isNewSeries;
        s += "\n\t\t\t\tis_new_in_destination:" + isNewInDestination;
        return s;
    }
}