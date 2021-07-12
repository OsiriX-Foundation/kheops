package online.kheops.auth_server.inbox;



import javax.xml.bind.annotation.XmlElement;
import java.util.*;

public class InboxInfoResponse {

    @XmlElement(name = "modalities")
    private SortedSet<String> modalities;
    @XmlElement(name = "number_of_studies")
    private Integer numberOfStudies;
    @XmlElement(name = "number_of_series")
    private Integer numberOfSeries;
    @XmlElement(name = "number_of_instances")
    private Integer numberOfInstances;

    private InboxInfoResponse() { /*empty*/ }

    public InboxInfoResponse(long nbStudies, long nbSeries, long nbInstances, String modalitiesLst) {
        this.numberOfStudies = ((Long) nbStudies).intValue();
        this.numberOfSeries = ((Long) nbSeries).intValue();
        this.numberOfInstances = ((Long) nbInstances).intValue();
        this.modalities = new TreeSet<>();
        if (modalitiesLst == null) {
            modalitiesLst = "";
        }
        this.modalities.addAll(Arrays.asList(modalitiesLst.substring(1, modalitiesLst.length()-1).split(",")));
    }

}
